package com.ustadmobile.libcache.downloader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RunDownloadJobUseCaseWorker(
    appContext: Context,
    params: WorkerParameters
) :CoroutineWorker(appContext, params), KoinComponent {

    private val runDownloadJobUseCase: RunDownloadJobUseCase by inject()

    override suspend fun doWork(): Result {
        return runWithJobRetry {
            runDownloadJobUseCase(
                inputData.getInt(
                    EnqueueRunDownloadJobUseCaseAndroid.JOB_UID, 0
                )
            )
        }
    }
}