package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.db.school.entities.WriteQueueItemEntity
import world.respect.datalayer.school.writequeue.WriteQueueItem

fun WriteQueueItemEntity.asModel() : WriteQueueItem{
    return WriteQueueItem(
        queueItemId = wqiQueueItemId,
        model = wqiModel,
        modelUidNum1 = wqiModelUidNum1,
        modelUidNum2 = wqiModelUidNum2,
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
        wqiModelUidNum1 = modelUidNum1,
        wqiModelUidNum2 = modelUidNum2,
        wqiTimestamp = timestamp,
        wqiAttemptCount = attemptCount,
        wqiAccountGuid = accountGuid,
        wqiTimeWritten = timeWritten,
    )
}
