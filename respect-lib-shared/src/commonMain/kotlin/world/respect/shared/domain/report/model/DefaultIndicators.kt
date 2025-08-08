package world.respect.shared.domain.report.model

import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.absence_percentage
import world.respect.shared.generated.resources.active_days_per_user
import world.respect.shared.generated.resources.attendance_percentage
import world.respect.shared.generated.resources.average_content_usage_duration_per_user
import world.respect.shared.generated.resources.average_score
import world.respect.shared.generated.resources.completion_rate
import world.respect.shared.generated.resources.number_of_activities
import world.respect.shared.generated.resources.offline_usage_percentage
import world.respect.shared.generated.resources.percentage_fail
import world.respect.shared.generated.resources.percentage_pass
import world.respect.shared.generated.resources.retention_rate
import world.respect.shared.generated.resources.task_completion_percentage
import world.respect.shared.generated.resources.top_5_apps
import world.respect.shared.generated.resources.top_5_languages
import world.respect.shared.generated.resources.top_5_lessons
import world.respect.shared.generated.resources.top_5_schools
import world.respect.shared.generated.resources.top_5_students
import world.respect.shared.generated.resources.total_content_usage_duration
import world.respect.shared.generated.resources.total_score


object DefaultIndicators {
    val list = listOf(
        // Duration Metrics
        Indicator(
            label = Res.string.total_content_usage_duration,
            type = YAxisTypes.DURATION
        ),
        Indicator(
            label = Res.string.average_content_usage_duration_per_user,
            type = YAxisTypes.DURATION
        ),

        // Percentage Metrics
        Indicator(
            label = Res.string.percentage_pass,
            type = YAxisTypes.PERCENTAGE
        ),
        Indicator(
            label = Res.string.percentage_fail,
            type = YAxisTypes.PERCENTAGE
        ),
        Indicator(
            label = Res.string.attendance_percentage,
            type = YAxisTypes.PERCENTAGE
        ),
        Indicator(
            label = Res.string.absence_percentage,
            type = YAxisTypes.PERCENTAGE
        ),
        Indicator(

            label = Res.string.completion_rate,
            type = YAxisTypes.PERCENTAGE
        ),
        Indicator(

            label = Res.string.task_completion_percentage,
            type = YAxisTypes.PERCENTAGE
        ),
        Indicator(

            label = Res.string.retention_rate,
            type = YAxisTypes.PERCENTAGE
        ),
        Indicator(
            label = Res.string.offline_usage_percentage,
            type = YAxisTypes.PERCENTAGE
        ),

        // Score Metrics
        Indicator(
            label = Res.string.average_score,
            type = YAxisTypes.COUNT
        ),
        Indicator(
            label = Res.string.total_score,
            type = YAxisTypes.COUNT
        ),

        // Count Metrics
        Indicator(
            label = Res.string.number_of_activities,
            type = YAxisTypes.COUNT
        ),
        Indicator(
            label = Res.string.active_days_per_user,
            type = YAxisTypes.COUNT
        ),

        // Top 5 Metrics
        Indicator(
            label = Res.string.top_5_apps,
            type = YAxisTypes.COUNT
        ),
        Indicator(
            label = Res.string.top_5_languages,
            type = YAxisTypes.COUNT
        ),
        Indicator(
            label = Res.string.top_5_lessons,
            type = YAxisTypes.COUNT
        ),
        Indicator(
            label = Res.string.top_5_schools,
            type = YAxisTypes.COUNT
        ),
        Indicator(
            label = Res.string.top_5_students,
            type = YAxisTypes.COUNT
        )
    )
}