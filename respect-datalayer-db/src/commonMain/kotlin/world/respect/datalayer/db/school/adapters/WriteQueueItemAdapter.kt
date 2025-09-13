package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.db.school.entities.WriteQueueItemEntity
import world.respect.datalayer.school.writequeue.WriteQueueItem

fun WriteQueueItemEntity.asModel() : WriteQueueItem{
    return WriteQueueItem(
        queueItemId = wqiQueueItemId,
        model = wqiModel,
        uid = wqiUid,
        timestamp = wqiTimestamp,
        attemptCount = wqiAttemptCount,
        timeWritten = wqiTimeWritten,
    )
}


fun WriteQueueItem.asEntity(
    accountGuid: String
): WriteQueueItemEntity {
    return WriteQueueItemEntity(
        wqiQueueItemId = queueItemId,
        wqiModel = model,
        wqiUid = uid,
        wqiTimestamp = timestamp,
        wqiAttemptCount = attemptCount,
        wqiAccountGuid = accountGuid,
        wqiTimeWritten = timeWritten,
    )
}
