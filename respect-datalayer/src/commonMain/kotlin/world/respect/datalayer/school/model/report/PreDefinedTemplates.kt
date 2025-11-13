package world.respect.datalayer.school.model.report

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import world.respect.datalayer.school.model.Report


object Templates {

    private fun parseDate(dateStr: String): Long {
        // Split "MM/dd/yyyy" format manually
        val parts = dateStr.split("/")
        val month = parts[0].toInt()
        val day = parts[1].toInt()
        val year = parts[2].toInt()

        return LocalDate(year, month, day)
            .atStartOfDayIn(TimeZone.UTC)
            .toEpochMilliseconds()
    }

    val list = listOf(
        Report(
            title = "Weekly active users",
            reportOptions = ReportOptions(
                title = "Weekly active users",
                xAxis = ReportXAxis.WEEK,
                period = ReportPeriodOption.LAST_WEEK.period,
                series = listOf(
                    ReportSeries(
                        reportSeriesTitle = "Students",
                        reportSeriesYAxis = DefaultIndicators.list.find { it.name == "Active days per user" } ?: DefaultIndicators.list.first(),
                        reportSeriesVisualType = ReportSeriesVisualType.LINE_GRAPH,
                    )
                )
            ),
            reportIsTemplate = true,
        ),
        Report(
            title = "Weekly duration",
            reportOptions = ReportOptions(
                title = "Weekly duration",
                xAxis = ReportXAxis.WEEK,
                period = ReportPeriodOption.LAST_WEEK.period,
                series = listOf(
                    ReportSeries(
                        reportSeriesTitle = "All users",
                        reportSeriesYAxis = DefaultIndicators.list.find { it.name == "Average content usage duration per user" }  ?: DefaultIndicators.list.first(),
                        reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                        reportSeriesSubGroup = ReportXAxis.CLASS
                    )
                )
            ),
            reportIsTemplate = true,
        ),
        Report(
            title = "Gender count",
            reportOptions = ReportOptions(
                title = "Gender count",
                xAxis = ReportXAxis.GENDER,
                period = ReportPeriodOption.LAST_30_DAYS.period,
                series = listOf(
                    ReportSeries(
                        reportSeriesTitle = "All users",
                        reportSeriesYAxis = DefaultIndicators.list.find { it.name == "Active days per user" }  ?: DefaultIndicators.list.first(),
                        reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                        reportSeriesFilters = listOf(
                            ReportFilter(
                                reportFilterField = FilterType.PERSON_GENDER,
                                reportFilterCondition = Comparisons.EQUALS,
                                reportFilterValue = "Male"
                            )
                        )
                    )
                )
            ),
            reportIsTemplate = true,
        ),
        Report(
            title = "Activities done",
            reportOptions = ReportOptions(
                title = "Activities done",
                xAxis = ReportXAxis.MONTH,
                period = FixedReportTimeRange(
                    fromDateMillis = parseDate("10/10/2025"),
                    toDateMillis = parseDate("10/23/2025")
                ),
                series = listOf(
                    ReportSeries(
                        reportSeriesTitle = "Students",
                        reportSeriesYAxis = DefaultIndicators.list.find { it.name == "Number of activities" }  ?: DefaultIndicators.list.first(),
                        reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                    )
                )
            ),
            reportIsTemplate = true,
        ),
        Report(
            title = "Top 5 students",
            reportOptions = ReportOptions(
                title = "Top 5 Students",
                xAxis = ReportXAxis.GENDER,
                period = RelativeRangeReportPeriod(
                    ReportTimeRangeUnit.WEEK,
                    rangeQuantity = 1
                ),
                series = listOf(
                    ReportSeries(
                        reportSeriesTitle = "Top 5",
                        reportSeriesYAxis = DefaultIndicators.list.find { it.name == "Total score" }  ?: DefaultIndicators.list.first(),
                        reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                        reportSeriesSubGroup = ReportXAxis.CLASS
                    )
                )
            ),
            reportIsTemplate = true,
        )
    )
}