package world.respect.datalayer.school.writequeue

/**
 * Represents an item in the write queue. The repository adds items into the the write queue
 * which are then drained by its send job.
 *
 * @property model the model that is to be sent.
 * @property uid the uid of the item that needs to be written to the remote datasource.
 * @property timeQueued the time the item was queued
 * @property timeWritten the time written to the remote datasource. If 0, then still pending
 */
class WriteQueueItem(
    val queueItemId: Int = 0,
    val model: Model,
    val uid: String,
    val timeQueued: Long = 0,
    val attemptCount: Int = 0,
    val timeWritten: Long = 0,
) {

    enum class Model(
        val flag: Int
    ) {
        PERSON(1), CLASS(2), ENROLLMENT(3), PERSON_PASSWORD(4),
        ASSIGNMENT(5), SCHOOL_APP(6), SCHOOL_PERMISSION_GRANT(7)
        , INVITE(8);

        companion object {

            fun fromFlag(flag: Int) = entries.first { it.flag == flag }

        }
    }

}