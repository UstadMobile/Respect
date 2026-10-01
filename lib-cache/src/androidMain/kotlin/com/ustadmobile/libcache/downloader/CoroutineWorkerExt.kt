package com.ustadmobile.libcache.downloader

import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import io.github.aakira.napier.Napier

suspend fun CoroutineWorker.runWithJobRetry(
    logFailureMessage: () -> String,
    maxAttempts: Int = 5,
    block: suspend () -> Unit,
) : ListenableWorker.Result {
    return try {
        block()
        ListenableWorker.Result.success()
    }catch(t: Exception) {
        Napier.w(throwable = t, message = logFailureMessage)
        if(runAttemptCount < maxAttempts) {
            ListenableWorker.Result.retry()
        }else {
            ListenableWorker.Result.failure()
        }
    }
}
