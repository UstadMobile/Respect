package world.respect.datalayer.school.model.report

import world.respect.datalayer.school.model.Indicator

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
            sql = "SUM(ResultSource.resultDuration) / COUNT(DISTINCT ResultSource.contextRegistrationHash)"
        ),

        // Percentage Metrics
        Indicator(
            name = "% Pass",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The proportion of students who successfully achieved passing grades on assessments or completed learning objectives successfully",
            sql = "SUM(CASE WHEN ResultSource.resultSuccess = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)"
        ),
        Indicator(
            name = "% Fail",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The percentage of students who did not meet the minimum requirements for passing assessments or completing learning modules",
            sql = "SUM(CASE WHEN ResultSource.resultSuccess = 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)"
        ),
        Indicator(
            name = "Attendance %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The rate of student presence in classes or learning sessions compared to total scheduled sessions",
            sql = "COUNT(DISTINCT ResultSource.statementActorPersonUid) * 100.0 / (SELECT COUNT(*) FROM PersonEntity WHERE pRole = 'student')"
        ),
        Indicator(
            name = "Absence %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The percentage of missed classes or learning sessions out of total scheduled educational activities",
            sql = "100.0 - (COUNT(DISTINCT ResultSource.statementActorPersonUid) * 100.0 / (SELECT COUNT(*) FROM PersonEntity WHERE pRole = 'student'))"
        ),
        Indicator(
            name = "Completion Rate",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The proportion of students who finished assigned courses, modules, or learning paths compared to those who started them",
            sql = "SUM(CASE WHEN ResultSource.resultCompletion = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)"
        ),
        Indicator(
            name = "Completion per Assigned Task %",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The percentage of successfully completed assignments, exercises, or learning activities out of all tasks assigned to students",
            sql = "SUM(CASE WHEN ResultSource.resultCompletion = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)"
        ),
        Indicator(
            name = "Retention Rate / Content Revisited Rate",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The frequency at which learners return to review previously covered material, indicating knowledge reinforcement and long-term retention",
            sql = "COUNT(DISTINCT ResultSource.statementActorPersonUid) * 100.0 / (SELECT COUNT(DISTINCT statementActorPersonUid) FROM StatementEntity WHERE timestamp < ?)"
        ),
        Indicator(
            name = "Offline Usage % vs. Online",
            type = YAxisTypes.PERCENTAGE.name,
            description = "The comparative usage of educational resources in offline mode versus online connectivity, showing accessibility patterns",
            sql = "SUM(CASE WHEN ResultSource.contextMode = 'offline' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)"
        ),

        // Count Metrics
        Indicator(
            name = "Score (Average)",
            type = YAxisTypes.COUNT.name,
            description = "The mean performance score across all assessments, tests, or evaluations for a given student group or time period",
            sql = "AVG(ResultSource.resultScore)"
        ),
        Indicator(
            name = "Score (Total)",
            type = YAxisTypes.COUNT.name,
            description = "The sum of all points earned by students across multiple assessments or cumulative performance measurements",
            sql = "SUM(ResultSource.resultScore)"
        ),
        Indicator(
            name = "Number of activities",
            type = YAxisTypes.COUNT.name,
            description = "The total count of learning exercises, assignments, quizzes, or educational tasks completed by students",
            sql = "COUNT(*)"
        ),
        Indicator(
            name = "Average Time Spent",
            type = YAxisTypes.COUNT.name,
            description = "The mean duration students dedicate to learning activities, measured in minutes per session or activity",
            sql = "AVG(ResultSource.resultDuration) / 60000.0" // Convert ms to minutes
        ),
        Indicator(
            name = "Active Days per User",
            type = YAxisTypes.COUNT.name,
            description = "The average number of days each student engages with learning materials within a specific time period",
            sql = "COUNT(DISTINCT strftime('%Y-%m-%d', ResultSource.timestamp/1000, 'unixepoch')) * 1.0 / COUNT(DISTINCT ResultSource.statementActorPersonUid)"
        ),

        // Top 5 Metrics - These need special handling as they return multiple rows
        Indicator(
            name = "Top 5: Apps",
            type = YAxisTypes.COUNT.name,
            description = "The five most frequently used educational applications based on user engagement time and activity frequency",
            sql = "COUNT(*) GROUP BY ResultSource.contextAppName ORDER BY COUNT(*) DESC LIMIT 5"
        ),
        Indicator(
            name = "Top 5: Languages",
            type = YAxisTypes.COUNT.name,
            description = "The five most commonly used languages for learning content consumption and interaction across the platform",
            sql = "COUNT(*) GROUP BY ResultSource.contextLanguage ORDER BY COUNT(*) DESC LIMIT 5"
        ),
        Indicator(
            name = "Top 5: Lessons",
            type = YAxisTypes.COUNT.name,
            description = "The five most accessed and completed educational lessons based on student participation and completion rates",
            sql = "COUNT(*) GROUP BY ResultSource.objectId ORDER BY COUNT(*) DESC LIMIT 5"
        ),
        Indicator(
            name = "Top 5: Schools",
            type = YAxisTypes.COUNT.name,
            description = "The five educational institutions with the highest levels of student engagement, content usage, or academic performance",
            sql = "COUNT(*) GROUP BY ResultSource.contextSchoolUid ORDER BY COUNT(*) DESC LIMIT 5"
        ),
        Indicator(
            name = "Top 5: Students",
            type = YAxisTypes.COUNT.name,
            description = "The five students demonstrating exceptional performance, high engagement levels, or outstanding academic achievement",
            sql = "COUNT(*) GROUP BY ResultSource.statementActorPersonUid ORDER BY COUNT(*) DESC LIMIT 5"
        )
    )
}