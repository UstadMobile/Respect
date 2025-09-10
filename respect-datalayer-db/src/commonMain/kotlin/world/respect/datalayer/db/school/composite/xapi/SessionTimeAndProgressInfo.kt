package world.respect.datalayer.db.school.composite.xapi

import kotlinx.serialization.Serializable

@Serializable
data class SessionTimeAndProgressInfo(
    var contextRegistrationHi: Long = 0,
    var contextRegistrationLo: Long = 0,
    var timeStarted: Long = 0,
    var maxProgress: Int? = null,
    var maxScore: Float? = null,
    var isCompleted: Boolean = false,
    var isSuccessful: Boolean? = null,
    var resultDuration: Long = 0,
)

object SessionTimeAndProgressInfoConst{
    const val SORT_BY_TIMESTAMP_DESC = 1

    const val SORT_BY_TIMESTAMP_ASC = 2


    const val SORT_BY_SCORE_ASC = 3
    const val SORT_BY_SCORE_DESC = 4

    const val SORT_BY_COMPLETION_ASC = 5
    const val SORT_BY_COMPLETION_DESC = 6

    const val SORT_BY_LEAST_RECENT_DESC = 7
    const val SORT_BY_LEAST_RECENT_ASC = 8

}