package com.ustadmobile.libcache.downloader

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class EnqueueRunDownloadJobUseCaseAndroid(
    private val appContext: Context
) : EnqueueRunDownloadJobUseCase{

    override fun invoke(downloadJobUid: Int) {
        val workRequest = OneTimeWorkRequestBuilder<RunDownloadJobUseCaseWorker>()
            .setInputData(
                Data.Builder()
                    .putInt(JOB_UID, downloadJobUid)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            uniqueWorkName = "$UNIQUE_NAME_PREFIX-$downloadJobUid",
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = workRequest,
        )
    }

    companion object {

        const val JOB_UID = "jobUid"

        const val UNIQUE_NAME_PREFIX = "rundownloadjob"

    }
}