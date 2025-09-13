package world.respect.datalayer.school.writequeue

/**
 * Represents an item in the write queue. The repository adds items into the the write queue
 * which are then drained by its send job.
 *
 * @property timeWritten the time written to the remote datasource. If 0, then still pending
 */
class WriteQueueItem(
    val queueItemId: Int = 0,
    val model: Model,
    val modelUidNum1: Long,
    val modelUidNum2: Long = 0,
    val timestamp: Long = 0,
    val attemptCount: Int = 0,
    val timeWritten: Long = 0,
) {

    enum class Model(
        val flag: Int
    ) {
        PERSON(1), CLASS(2), ENROLLMENT(3);

        companion object {

            fun fromFlag(flag: Int) = entries.first { it.flag == flag }

        }
    }

}