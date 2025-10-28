package world.respect.datalayer.db.school.domain.query

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.domain.report.query.GenerateReportQueriesUseCase
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCaseDatabaseImpl
import world.respect.datalayer.school.model.report.DefaultIndicators
import world.respect.datalayer.school.model.report.ReportOptions
import world.respect.datalayer.school.model.report.ReportPeriodOption
import world.respect.datalayer.school.model.report.ReportSeries
import world.respect.datalayer.school.model.report.ReportXAxis
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
//        runBlocking { schoolDb.insertStatementsPerDay() }

        val results = runBlocking {
            runReportUseCase(
                request = RunReportUseCase.RunReportRequest(
                    reportUid = 42L,
                    reportOptions = ReportOptions(
                        xAxis = ReportXAxis.DAY,
                        series = listOf(
                            ReportSeries(
                                reportSeriesYAxis = DefaultIndicators.list.first(),
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
        assertEquals(7, seriesResults.size,
            "result size equals number of days of reporting period - LAST_WEEK - 7 days")

//        (0 until DEFAULT_NUM_DAYS).forEach { dayIndex ->
//            val localDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
//                .minus(DatePeriod(days = dayIndex))
//
//            assertEquals(
//                expected = (DEFAULT_DURATION_PER_STATEMENT * DEFAULT_NUM_STATEMENTS_PER_DAY).toDouble(),
//                actual = seriesResults.find { it.xAxis == localDate.toString() }!!.yAxis,
//                message = "day $dayIndex has expected total duration"
//            )
//        }
    }
}