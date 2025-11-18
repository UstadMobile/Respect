package com.ustadmobile.libcache.sendinvite

import android.content.Context
import android.content.Intent
import com.ustadmobile.libcache.sharelink.ShareLinkLauncher
import com.ustadmobile.libcache.sharelink.ShareLinkLauncher.Companion.MIME_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShareLinkLauncherAndroid(
    private val context: Context
) : ShareLinkLauncher {


    override suspend fun launch(link: String) = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE
                putExtra(Intent.EXTRA_TEXT, link)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(intent, null).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(chooser)
        } catch (_: Throwable) { }
    }
}
