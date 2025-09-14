package world.respect.datalayer.repository.school.writequeue

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DrainRemoteWriteQueueWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params){

    override suspend fun doWork(): Result {
        return Result.success()
    }

}