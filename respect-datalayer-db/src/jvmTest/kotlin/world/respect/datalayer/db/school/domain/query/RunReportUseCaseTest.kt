package world.respect.datalayer.db.school.domain.query

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.domain.report.query.GenerateReportQueriesUseCase
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCaseDatabaseImpl
import world.respect.datalayer.school.model.report.DefaultIndicators
import world.respect.datalayer.school.model.report.RelativeRangeReportPeriod
import world.respect.datalayer.school.model.report.ReportOptions
import world.respect.datalayer.school.model.report.ReportPeriodOption
import world.respect.datalayer.school.model.report.ReportSeries
import world.respect.datalayer.school.model.report.ReportTimeRangeUnit
import world.respect.datalayer.school.model.report.ReportXAxis
import world.respect.lib.test.clientservertest.ext.DEFAULT_DURATION_PER_STATEMENT
import world.respect.lib.test.clientservertest.ext.DEFAULT_NUM_DAYS
import world.respect.lib.test.clientservertest.ext.DEFAULT_NUM_STATEMENTS_PER_DAY
import world.respect.lib.test.clientservertest.ext.DEFAULT_STATEMENT_CLAZZ_UID
import world.respect.lib.test.clientservertest.ext.insertStatementsPerDay
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class RunReportUseCaseTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var schoolDb: RespectSchoolDatabase

    private lateinit var runReportUseCase: RunReportUseCase

    @BeforeTest
    fun setup() {
        val dbDir = temporaryFolder.newFolder("dbdir")
        schoolDb = Room.databaseBuilder<RespectSchoolDatabase>(
            File(dbDir, "realm-test.db").absolutePath
        ).setDriver(BundledSQLiteDriver())
            .build()
        runReportUseCase = RunReportUseCaseDatabaseImpl(
            schoolDatabase = schoolDb,
            generateReportQueriesUseCase = GenerateReportQueriesUseCase()
        )
    }

    private val defaultAccountPersonUid = 1L

    private val defaultStatementClazzUid = 42L

    @Test
    fun givenStatementsInDatabase_whenDurationPerDayQueried_thenResultsAsExpected() {
        runBlocking { schoolDb.insertStatementsPerDay() }

        val results = runBlocking {
            runReportUseCase(
                request = RunReportUseCase.RunReportRequest(
                    reportUid = 42L,
                    reportOptions = ReportOptions(
                        xAxis = ReportXAxis.DAY,
                        series = listOf(
                            ReportSeries(
                                reportSeriesYAxis = DefaultIndicators.list.first(),
                                reportSeriesSubGroup = null
                            )
                        ),
                        period = ReportPeriodOption.LAST_WEEK.period,
                    ),
                    accountPersonUid = defaultAccountPersonUid,
                    timeZone = TimeZone.UTC,
                )
            ).first()
        }

        // Get the first series results (since we only have one series)
        val seriesResults = results.results.first()

        // Verify we have exactly 7 days of data
        assertEquals(
            7, seriesResults.size,
            "result size equals number of days of reporting period - LAST_WEEK - 7 days"
        )

        (0 until DEFAULT_NUM_DAYS).forEach { dayIndex ->
            val localDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
                .minus(DatePeriod(days = dayIndex))

            assertEquals(
                expected = (DEFAULT_DURATION_PER_STATEMENT * DEFAULT_NUM_STATEMENTS_PER_DAY).toDouble(),
                actual = seriesResults.find { it.xAxis == localDate.toString() }!!.yAxis,
                message = "day $dayIndex has expected total duration"
            )
        }
    }

    @Test
    fun givenStatementsInDatabase_whenDurationPerWeekQueried_thenResultsAsExpected() {
        val numWeeks = 3
        val numDaysStatements = numWeeks * 7
        runBlocking { schoolDb.insertStatementsPerDay(numDays = numDaysStatements) }

        val request = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions(
                xAxis = ReportXAxis.WEEK,
                series = listOf(
                    ReportSeries(
                        reportSeriesYAxis = DefaultIndicators.list.first(),
                        reportSeriesSubGroup = null
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.WEEK, 3),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request).first()
        }.results.first()

        assertEquals(
            numWeeks, results.size,
            "result size equals number of weeks of reporting period - $numWeeks weeks"
        )

        (0 until numWeeks).forEach { weekNum ->
            val firstDayOfWeek = Instant.fromEpochMilliseconds(
                request.reportOptions.period.periodStartMillis(request.timeZone)
            ).toLocalDateTime(request.timeZone)
                .date.plus(DatePeriod(days = weekNum * 7))
            val row = results.firstOrNull { it.xAxis == firstDayOfWeek.toString() }

            assertEquals(
                expected = (DEFAULT_DURATION_PER_STATEMENT * DEFAULT_NUM_STATEMENTS_PER_DAY * 7).toDouble(),
                actual = row?.yAxis ?: -1.0,
                message = "week $weekNum has expected total duration"
            )
        }
    }

    @Test
    fun givenStatementsInDatabase_whenDurationPerMonthQueried_thenReturnsExpectedNumOfResults() {
        val numDaysStatements = 90

        runBlocking { schoolDb.insertStatementsPerDay(numDays = numDaysStatements) }

        val reportNumMonths = 3
        val request = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions(
                xAxis = ReportXAxis.MONTH,
                series = listOf(
                    ReportSeries(
                        reportSeriesYAxis = DefaultIndicators.list.first(),
                        reportSeriesSubGroup = null
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.MONTH, reportNumMonths),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request).first()
        }.results.first()

        assertEquals(
            reportNumMonths, results.size,
            "result size equals number of weeks of reporting period - 3 months"
        )
        assertTrue(
            results.all { it.xAxis.endsWith("01") },
            "Report by month xAxis should always end with 01 (e.g. first of month)"
        )
    }


    @Test
    fun givenStatementsInDatabase_whenDurationPerYearQueried_thenReturnsExpectedNumOfResults() {
        val reportNumYears = 2
        val numDaysStatements = 365 * reportNumYears

        runBlocking { schoolDb.insertStatementsPerDay(numDays = numDaysStatements) }

        val request = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions(
                xAxis = ReportXAxis.YEAR,
                series = listOf(
                    ReportSeries(
                        reportSeriesYAxis = DefaultIndicators.list.first(),
                        reportSeriesSubGroup = null
                    )
                ),
                period = RelativeRangeReportPeriod(ReportTimeRangeUnit.YEAR, reportNumYears),
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(request = request).first()
        }.results.first()

        assertEquals(
            reportNumYears, results.size,
            "result size equals number of weeks of reporting period - 2 years"
        )
        assertTrue(
            results.all { it.xAxis.endsWith("01-01") },
            "Report by year xAxis should always end with 01-01 (e.g. first day of the year)"
        )
    }

    @Test
    fun givenAllReportOptionCombinations_whenRun_thenShouldNotThrowException() {
        runBlocking { schoolDb.insertStatementsPerDay() }

        runBlocking {
            DefaultIndicators.list.forEach { yAxis ->
                ReportXAxis.entries.forEach { xAxis ->
                    try {
                        runReportUseCase(
                            request = RunReportUseCase.RunReportRequest(
                                reportUid = 42L,
                                reportOptions = ReportOptions(
                                    xAxis = xAxis,
                                    series = listOf(
                                        ReportSeries(
                                            reportSeriesYAxis = yAxis
                                        )
                                    ),
                                    period = ReportPeriodOption.LAST_WEEK.period,
                                ),
                                accountPersonUid = defaultAccountPersonUid,
                                timeZone = TimeZone.UTC,
                            )
                        )
                    } catch (e: Throwable) {
                        println("Exception running report yAxis=$yAxis xAxis=$xAxis")
                        throw e
                    }
                }
            }
        }
    }

    @Test
    fun givenReportOptionsWithSubgroup_whenRun_thenResultsAsExpected() {
        runBlocking {
            schoolDb.insertStatementsPerDay(
                statementClazzUid = {
                    defaultStatementClazzUid + it.mod(2)
                }
            )
        }

        val results = runBlocking {
            runReportUseCase(
                request = RunReportUseCase.RunReportRequest(
                    reportUid = 42L,
                    reportOptions = ReportOptions(
                        xAxis = ReportXAxis.DAY,
                        series = listOf(
                            ReportSeries(
                                reportSeriesYAxis = DefaultIndicators.list.first(),
                                reportSeriesSubGroup = ReportXAxis.CLASS,
                            )
                        ),
                        period = ReportPeriodOption.LAST_WEEK.period,
                    ),
                    accountPersonUid = defaultAccountPersonUid,
                    timeZone = TimeZone.UTC,
                )
            ).first()
        }.results.first()

        //When using subgrouping, for each xAxis day, there should be two results (one per clazzUid value).
        (0 until DEFAULT_NUM_DAYS).forEach { dayIndex ->
            listOf(
                DEFAULT_STATEMENT_CLAZZ_UID,
                DEFAULT_STATEMENT_CLAZZ_UID + 1
            ).forEach { clazzUid ->
                val localDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
                    .minus(DatePeriod(days = dayIndex))

                assertEquals(
                    expected = (DEFAULT_DURATION_PER_STATEMENT * (DEFAULT_NUM_STATEMENTS_PER_DAY / 2)).toDouble(),
                    actual = results.find {
                        it.xAxis == localDate.toString() && it.subgroup == clazzUid.toString()
                    }!!.yAxis,
                    message = "day $dayIndex has expected total duration"
                )
            }
        }
    }

    @Test
    fun givenReportIsFresh_whenRunAgain_thenCacheResultReturned() {
        runBlocking { schoolDb.insertStatementsPerDay() }
        val runReportRequest = RunReportUseCase.RunReportRequest(
            reportUid = 42L,
            reportOptions = ReportOptions(
                xAxis = ReportXAxis.DAY,
                series = listOf(
                    ReportSeries(
                        reportSeriesYAxis = DefaultIndicators.list.first(),
                        reportSeriesSubGroup = null
                    )
                ),
                period = ReportPeriodOption.LAST_WEEK.period,
            ),
            accountPersonUid = defaultAccountPersonUid,
            timeZone = TimeZone.UTC,
        )

        val results = runBlocking {
            runReportUseCase(
                request = runReportRequest
            ).first()
        }

        Thread.sleep(1000)

        val cachedResults = runBlocking {
            runReportUseCase(
                request = runReportRequest
            ).first()
        }

        assertEquals(
            7, cachedResults.results.first().size,
            "result size equals number of days of reporting period - LAST_WEEK - 7 days"
        )
        assertTrue(cachedResults.age > 0, "Cached results age > 0")
        assertEquals(0, results.age)
    }
}