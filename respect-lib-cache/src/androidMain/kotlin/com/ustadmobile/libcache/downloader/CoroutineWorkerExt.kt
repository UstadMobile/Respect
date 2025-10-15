package com.ustadmobile.libcache.downloader

import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker

suspend fun CoroutineWorker.runWithJobRetry(
    maxAttempts: Int = 5,
    block: suspend () -> Unit,
) : ListenableWorker.Result {
    return try {
        block()
        ListenableWorker.Result.success()
    }catch(e: Exception) {
        if(runAttemptCount < maxAttempts) {
            ListenableWorker.Result.retry()
        }else {
            ListenableWorker.Result.failure()
        }
    }
}
