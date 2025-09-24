package world.respect.datalayer.repository.school.writequeue

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.TypeQualifier
import world.respect.datalayer.school.writequeue.EnqueueDrainRemoteWriteQueueUseCase

class DrainRemoteWriteQueueWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        val scopeId = inputData.getString(EnqueueDrainRemoteWriteQueueUseCase.DATA_SCOPE_ID)
            ?: throw IllegalStateException()
        val scopeQualifierName = Class.forName(
            inputData.getString(EnqueueDrainRemoteWriteQueueUseCase.DATA_SCOPE_QUALIFIER)!!
        ).kotlin

        val logPrefix: String by lazy { "DrainRemoteWriteQueueWorker(Scope=$scopeId)" }

        val scope = getKoin().getOrCreateScope(scopeId, TypeQualifier(scopeQualifierName))
        val drainRemoteWriteQueueUseCase: DrainRemoteWriteQueueUseCase = scope.get()

        return try {
            drainRemoteWriteQueueUseCase()
            Napier.d("$logPrefix completed successfully")
            Result.success()
        }catch(e: Throwable) {
            Napier.w("$logPrefix error", e)
            Result.failure()
        }
    }

}