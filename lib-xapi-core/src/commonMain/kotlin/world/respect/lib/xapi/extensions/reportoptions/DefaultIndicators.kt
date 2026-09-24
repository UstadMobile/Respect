package world.respect.lib.xapi.extensions.reportoptions

//TODO Need to change with string resource

object DefaultIndicators {
    val list = listOf(
        // Duration Metrics
        Indicator(
            name = "Total content usage duration",
            type = YAxisTypes.DURATION.name,
            description = "The cumulative amount of time spent by all users engaging with educational content, measured in hours and minutes",
            sql = "SUM(ResultSource.resultDuration)"
        ),
        Indicator(
            name = "Average content usage duration per user",
            type = YAxisTypes.DURATION.name,
            description = "The mean time spent by individual users interacting with learning materials, calculated as total usage time divided by number of active users",
            sql = "SUM(ResultSource.resultDuration) /" +
                    "COUNT(DISTINCT ResultSource.contextRegistrationHash)"
        ),


        // Count Metrics
        Indicator(
            name = "Score (Average)",
            type = YAxisTypes.COUNT.name,
            description = "The mean performance score across all assessments, tests, or evaluations for a given student group or time period",
            sql = "AVG(ResultSource.resultScoreScaled)"
        ),
        Indicator(
            name = "Score (Total)",
            type = YAxisTypes.COUNT.name,
            description = "The sum of all points earned by students across multiple assessments or cumulative performance measurements",
            sql = "SUM(ResultSource.resultScoreScaled)"
        ),
        Indicator(
            name = "Number of Unique Users",
            type = YAxisTypes.COUNT.name,
            description = "The count of distinct users who engaged with the learning content during the specified time period",
            sql = "COUNT(DISTINCT CASE WHEN (SELECT actorObjectType FROM XapiActorEntity WHERE actorUid = ResultSource.statementActorUid) = 1 THEN ResultSource.statementActorUid END)",
        ),

    )
}