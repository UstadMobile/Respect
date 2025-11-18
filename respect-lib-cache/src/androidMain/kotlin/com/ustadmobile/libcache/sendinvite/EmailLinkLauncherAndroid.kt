package com.ustadmobile.libcache.sendinvite

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.ustadmobile.libcache.sharelink.EmailLinkLauncher
import com.ustadmobile.libcache.sharelink.EmailLinkLauncher.Companion.SCHEME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailLinkLauncherAndroid(
    private val context: Context
) : EmailLinkLauncher {


    override suspend fun launch(inviteLink: String) = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, SCHEME.toUri()).apply {
                putExtra(Intent.EXTRA_SUBJECT, "Invitation")
                putExtra(Intent.EXTRA_TEXT, inviteLink)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Throwable) { }
    }
}
