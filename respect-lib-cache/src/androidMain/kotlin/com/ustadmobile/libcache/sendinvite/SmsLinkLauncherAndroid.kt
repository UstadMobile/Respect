package com.ustadmobile.libcache.sendinvite

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.ustadmobile.libcache.sharelink.SmsLinkLauncher
import com.ustadmobile.libcache.sharelink.SmsLinkLauncher.Companion.EXTRA_SMS_BODY
import com.ustadmobile.libcache.sharelink.SmsLinkLauncher.Companion.SMS_URI_SCHEME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsLinkLauncherAndroid(private val context: Context) : SmsLinkLauncher {
    override suspend fun sendLink(inviteLink: String) = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, SMS_URI_SCHEME.toUri()).apply {
                putExtra(EXTRA_SMS_BODY, inviteLink)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
        }
    }
}
