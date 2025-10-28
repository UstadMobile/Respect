package world.respect.datalayer.db.school.domain.report.query

import kotlinx.datetime.offsetAt
import world.respect.datalayer.ext.DatabaseType
import world.respect.datalayer.ext.PermissionFlags
import world.respect.datalayer.school.model.report.ReportXAxis
import world.respect.libutil.util.time.systemTimeInMillis

/**
 * `GenerateReportQueriesUseCase`
 *
 * This class is responsible for generating the SQL queries required to populate a report
 * based on the provided `RunReportUseCase.RunReportRequest`. It handles different database types
 * (SQLite and Postgres) and various report options such as the x-axis (time-based or categorical),
 * y-axis (metrics), and sub-grouping. This is normally something like 'total usage duration by week'
 * (see ReportOptions2 for available xAxis/yAxis options).
 *
 * ReportOptions2 allows the user to select the YAxis, XAXis, subgrouping (if any), time period, etc.
 *
 * The SQL uses the GROUP BY clause to aggregate data according to XAxis and subgrouping (if any).
 * Once data is grouped, we can use SQL aggregate functions (e.g. SUM, AVERAGE, COUNT etc) to get the
 * yAxis corresponding with each xAxis/subgroup combination.
 *
 * The results of the query will be inserted into the ReportQueryResult table which essentially
 * acts as a cache. Report queries can be long/complex; hence it is necessary to cache the results,
 * even on the server side.
 */
class GenerateReportQueriesUseCase {

    /**
     * @param sql The SQL to run (including ? placeholders for any parameters)
     * @param params The parameters values to use
     * @param timestamp timestamp - used for the current time (e.g. ReportQueryResult.rqrLastModified)
     *        This may be needed as the basis for subsequent calculations; and is therefor included
     *        in the return value.
     */
    data class ReportQueryParts2(
        val sql: String,
        val params: Array<Any>,
        val timestamp: Long,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ReportQueryParts2) return false

            if (timestamp != other.timestamp) return false
            if (sql != other.sql) return false
            if (!params.contentEquals(other.params)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = timestamp.hashCode()
            result = 31 * result + sql.hashCode()
            result = 31 * result + params.contentHashCode()
            return result
        }
    }

    private fun xAxisOrSubgroupExpression(
        field: ReportXAxis,
        dbType: Int,
        request: RunReportUseCase.RunReportRequest,
    ) : String {
        val reportOptions = request.reportOptions
        val timeZone = request.timeZone

        /**
         * Database functions will convert ms since epoch into dates using the UTC timezone: we want
         * to get the date as per the RunReportRequest timezone. Therefor we add the timezone offset
         * such that the SQLite/Postgres functions will return the date as expected.
         *
         * This methodology doesn't handle daylight saving time changes within the report period.
         */
        val offsetMillis = timeZone.offsetAt(reportOptions.period.periodStartInstant(timeZone))
            .totalSeconds * 1000

        val timeFieldName = "(ResultSource.timestamp + $offsetMillis)"

        /*
         * strftime should be able to use %F to create an iso formatted date, unfortunately, this
         * doesn't work across all SQLite versions, so '%Y-%m-%d' is used instead
         *
         * See https://www.sqlite.org/lang_datefunc.html
         */
        return buildString {
            when(field) {
                ReportXAxis.DAY -> {
                    append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch')")
                }

                ReportXAxis.WEEK -> {
                            /* Group data as documented on ReportXAxis.WEEK - on SQLite this works
                             * by :
                             *  a) When the day of week for the timestamp matches
                             *     StartOfWeekCte.TimeRangeStartDayOfWeek, just format the timestamp date
                             *  b) if not, use the weekday modifier which will advance the date
                             *     forward to when the day of week will match, then adjust backwards
                             *     7 days, thus grouping data by week commencing date.
                             */
                            append("""
                              (CASE strftime('%w', $timeFieldName/1000, 'unixepoch')
                                    WHEN (SELECT StartOfWeekCte.TimeRangeStartDayOfWeek
                                           FROM StartOfWeekCte)
                                	THEN strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch')
                                
                                	ELSE strftime('%Y-%m-%d', $timeFieldName/1000, 
                                                  'unixepoch', 
                                                  'weekday ' || 
                                                  (SELECT StartOfWeekCte.TimeRangeStartDayOfWeek
                                                     FROM StartOfWeekCte), 
                                                  '-7 day')
                                 END)
                            """.trimIndent())
                }

                ReportXAxis.MONTH -> {
                            append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch', 'start of month') ")

                }

                ReportXAxis.YEAR -> {

                            append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch', 'start of year') ")
                }

                ReportXAxis.CLASS -> {
                    append("ResultSource.statementClazzUid")
                }

                ReportXAxis.GENDER -> {
                    append("COALESCE(Person.gender, 0)")
                }

                ReportXAxis.QUARTER -> TODO()
                ReportXAxis.TIME_OF_DAY -> TODO()
                ReportXAxis.SUBJECT -> TODO()
                ReportXAxis.SCHOOL -> TODO()
                ReportXAxis.ASSESSMENT_TYPE -> TODO()
                ReportXAxis.GRADE_LEVEL -> TODO()
                ReportXAxis.AGE_GROUP -> TODO()
                ReportXAxis.REGION -> TODO()
                ReportXAxis.LANGUAGE -> TODO()
                ReportXAxis.USER_ROLE -> TODO()
                ReportXAxis.ACTIVITY_VERB -> TODO()
                ReportXAxis.APPLICATION -> TODO()
                ReportXAxis.DEVICE_TYPE -> TODO()
                ReportXAxis.ROLE -> TODO()
            }
        }
    }

    operator fun invoke(
        request: RunReportUseCase.RunReportRequest,
        dbType: Int = DatabaseType.getCurrentDbType(), // Default to SQLite
    ): List<ReportQueryParts2> {
        val reportOptions = request.reportOptions
        val xAxis = reportOptions.xAxis ?: throw IllegalArgumentException("null x axis")

        val reportFromMs = reportOptions.period.periodStartMillis(request.timeZone)
        val reportToMs = reportOptions.period.periodEndMillis(request.timeZone)
        val timenow = systemTimeInMillis()

        return reportOptions.series.map { series ->
            val yAxis = series.reportSeriesYAxis
            var sql = ""
            val paramsList = mutableListOf<Any>()


            /* Permission check CTEs:
             * a) AllLearningRecordsPermission: Check if the user as per accountPersonUid has view
             *    learning records SystemPermission, in which case, no further checks will be
             *    required.
             * b) Where the user does not have the SystemPermission, get a list of the clazzUids for
             *    which the active user has view learning records permission
             */
            sql += """
                INSERT INTO ReportQueryResult(rqrReportUid, rqrLastModified,
                rqrReportSeriesUid, rqrLastValidated, rqrXAxis, rqrYAxis,
                rqrSubgroup, rqrTimeZone)
                
                WITH AllLearningRecordsPermission(hasPermission) AS (
                     SELECT EXISTS(
                            SELECT 1
                              FROM SystemPermission
                             WHERE SystemPermission.spToPersonUid = ?
                               AND (SystemPermission.spPermissionsFlag & ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}))
                            AS hasPermission   
                ),
                
                ClazzesWithPermission(clazzUid) AS(
                     SELECT Clazz.clazzUid AS clazzUid
                       FROM Clazz
                      WHERE NOT (SELECT hasPermission FROM AllLearningRecordsPermission)
                        AND Clazz.clazzOwnerPersonUid = ?
                      UNION
                      SELECT CoursePermission.cpClazzUid AS clazzUid
                        FROM CoursePermission
                       WHERE NOT (SELECT hasPermission FROM AllLearningRecordsPermission)
                         AND (    CoursePermission.cpToPersonUid = ?
                              OR ((CoursePermission.cpToEnrolmentRole, CoursePermission.cpClazzUid) IN
                                  (SELECT ClazzEnrolment.clazzEnrolmentRole,
                                          ClazzEnrolment.clazzEnrolmentClazzUid
                                     FROM ClazzEnrolment      
                                    WHERE ClazzEnrolment.clazzEnrolmentPersonUid = ?)))
                         AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}) > 0         
                )
            """.trimIndent()
            //All parameters above are the accountPersonUid (4)
            paramsList.addAll((0 until 4).map { request.accountPersonUid })

            /*
             * Where xAxis or subgrouping is by week and we are using SQLite we need to know the
             * first day of week; so this is added as a CTE.
             */
            if(
                dbType == DatabaseType.SQLITE &&
                (xAxis == ReportXAxis.WEEK ||
                        reportOptions.series.any { it.reportSeriesSubGroup == ReportXAxis.WEEK } )
            ) {
                sql += ",StartOfWeekCte(TimeRangeStartDayOfWeek) AS (\n" +
                        "SELECT strftime('%w', ?, 'unixepoch') AS TimeRangeStartDayOfWeek\n" +
                        ")\n"
                paramsList.add(reportFromMs/1000)
            }

            sql += ",ResultSourceCte AS (\n "
            sql += "SELECT "

            // Use the Indicator's SQL
            if (yAxis.sql.isBlank()) {
                throw IllegalArgumentException("Indicator ${yAxis.name} has no SQL defined")
            }
            sql += yAxis.sql + " AS yAxis,\n"

            sql += xAxisOrSubgroupExpression(xAxis, dbType, request) + " AS xAxis,\n"

            when(series.reportSeriesSubGroup) {
                null -> {
                    sql += "'' AS subgroup\n"
                }

                else -> {
                    sql += "${xAxisOrSubgroupExpression(series.reportSeriesSubGroup, dbType, request)} AS subgroup\n"
                }
            }

            sql += "FROM StatementEntity ResultSource\n"
            if(reportOptions.xAxis.personJoinRequired ||
                series.reportSeriesSubGroup?.personJoinRequired == true
            ) {
                sql += "LEFT JOIN Person\n" +
                        "ON Person.personUid = ResultSource.statementActorPersonUid\n"
            }

            sql += "WHERE ResultSource.timestamp BETWEEN ? AND ?\n"
            paramsList.add(reportFromMs)
            paramsList.add(reportToMs)

            sql += """
                AND (     (SELECT AllLearningRecordsPermission.hasPermission
                             FROM AllLearningRecordsPermission)
                       OR ResultSource.statementActorPersonUid = ? 
                       OR ResultSource.statementClazzUid IN 
                          (SELECT ClazzesWithPermission.clazzUid
                             FROM ClazzesWithPermission)
                    )         
            """.trimIndent()
            paramsList.add(request.accountPersonUid)

            sql += " GROUP BY xAxis"
            series.reportSeriesSubGroup?.also {
                sql += ", subgroup"
            }

            sql += "\n)\n"

            //Order must match INSERT clause
            sql += """
                SELECT ? AS rqrReportUid,
                       ? AS rqrLastModified,
                       ? AS rqrReportSeriesUid,
                       ? AS rqrLastValidated,
                       ResultSourceCte.xAxis AS rqrXAxis,
                       ResultSourceCte.yAxis AS rqrYAxis,
                       ResultSourceCte.subgroup AS rqrSubgroup,
                       ? AS rqrSubgroup
                  FROM ResultSourceCte  
            """.trimIndent()
            paramsList.addAll(
                listOf(request.reportUid, timenow, series.reportSeriesUid, timenow, request.timeZone.id)
            )

            ReportQueryParts2(sql, paramsList.toTypedArray(), timenow)
        }
    }
}