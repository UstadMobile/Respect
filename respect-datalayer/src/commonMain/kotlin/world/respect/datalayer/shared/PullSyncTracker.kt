package world.respect.datalayer.shared

import world.respect.datalayer.school.model.PullSyncStatus

interface PullSyncTracker {

    suspend fun getPullSyncStatus(
        tableId: Int,
    ): PullSyncStatus?

    suspend fun updatePullSyncStatus(
        status: PullSyncStatus,
    )

}
