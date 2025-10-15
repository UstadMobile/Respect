package com.ustadmobile.libcache.downloader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PinPublicationPrepareUseCaseWorker(
    private val appContext: Context,
    params: WorkerParameters
) :CoroutineWorker(appContext, params) {

    //private val pinPublicationPrepareUseCase: PinPublicationPrepareUseCase by inject()

    override suspend fun doWork(): Result {
        return Result.success()
    }
}