package com.ustadmobile.libcache.downloader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ustadmobile.libcache.downloader.EnqueuePinPublicationPrepareUseCaseAndroid.Companion.JOB_UID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PinPublicationPrepareUseCaseWorker(
    appContext: Context,
    params: WorkerParameters
) :CoroutineWorker(appContext, params), KoinComponent {

    private val pinPublicationPrepareUseCase: PinPublicationPrepareUseCase by inject()

    override suspend fun doWork(): Result {
        return runWithJobRetry(
            maxAttempts = PinPublicationPrepareUseCase.DEFAULT_MAX_ATTEMPTS,
            logFailureMessage = { "PinPublicationPrepareUseCaseWorker failed" }
        ) {
            pinPublicationPrepareUseCase(inputData.getInt(JOB_UID, 0))
        }
    }
}