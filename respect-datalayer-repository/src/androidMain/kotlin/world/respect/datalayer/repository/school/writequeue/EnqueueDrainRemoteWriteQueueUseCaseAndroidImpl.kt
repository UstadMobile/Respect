package world.respect.datalayer.repository.school.writequeue

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import world.respect.datalayer.school.writequeue.EnqueueDrainRemoteWriteQueueUseCase

class EnqueueDrainRemoteWriteQueueUseCaseAndroidImpl(
    private val context: Context,
    private val scopeId: String,
): EnqueueDrainRemoteWriteQueueUseCase {

    override suspend fun invoke() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName = "$UNIQUE_NAME_PREFIX-$scopeId",
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = OneTimeWorkRequestBuilder<DrainRemoteWriteQueueWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(Data.Builder()
                    .putString(DATA_SCOPE_ID, scopeId)
                    .build())
                .build()
        )
    }

    companion object {

        const val UNIQUE_NAME_PREFIX = "drainremotewrite"

        const val DATA_SCOPE_ID = "scopeId"

    }

}