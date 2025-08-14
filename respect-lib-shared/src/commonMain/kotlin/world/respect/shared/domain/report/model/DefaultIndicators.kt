package world.respect.shared.domain.report.model

import world.respect.datalayer.respect.model.Indicator

//TODO Need to change with string resource

object DefaultIndicators {
    val list = listOf(
        // Duration Metrics
        Indicator(
            name = "Total content usage duration",
            type = YAxisTypes.DURATION.name,
            description = "description"
        ),
        Indicator(
            name = "Average content usage duration per user",
            type = YAxisTypes.DURATION.name,
            description = "description"
        ),

        // Percentage Metrics
        Indicator(
            name = "% Pass",
            type = YAxisTypes.PERCENTAGE.name,
            description = "description"
        ),
        Indicator(
            name = "% Fail",
            type = YAxisTypes.PERCENTAGE.name,
            description = "description"
        ),
        Indicator(
            name = "Attendance %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "description"
        ),
        Indicator(
            name = "Absence %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "description"
        ),
        Indicator(
            name = "Completion Rate",
            type = YAxisTypes.PERCENTAGE.name,
            description = "description"
        ),
        Indicator(
            name = "Completion per Assigned Task %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "description"
        ),
        Indicator(
            name = "Retention Rate / Content Revisited Rate",
            type = YAxisTypes.PERCENTAGE.name
        ),
        Indicator(
            name = "Offline Usage % vs. Online",
            type = YAxisTypes.PERCENTAGE.name
        ),

        // Score Metrics
        Indicator(
            name = "Score (Average)",
            type = YAxisTypes.COUNT.name
        ),
        Indicator(
            name = "Score (Total)",
            type = YAxisTypes.COUNT.name
        ),

        // Count Metrics
        Indicator(
            name = "Number of activities",
            type = YAxisTypes.COUNT.name
        ),
        Indicator(
            name = "Average Time Spent",
            type = YAxisTypes.COUNT.name
        ),
        Indicator(
            name = "Active Days per User",
            type = YAxisTypes.COUNT.name
        ),

        // Top 5 Metrics
        Indicator(
            name = "Top 5: Apps",
            type = YAxisTypes.COUNT.name
        ),
        Indicator(
            name = "Top 5: Languages",
            type = YAxisTypes.COUNT.name
        ),
        Indicator(
            name = "Top 5: Lessons",
            type = YAxisTypes.COUNT.name
        ),
        Indicator(
            name = "Top 5: Schools",
            type = YAxisTypes.COUNT.name
        ),
        Indicator(
            name = "Top 5: Students",
            type = YAxisTypes.COUNT.name
        )
    )
}
