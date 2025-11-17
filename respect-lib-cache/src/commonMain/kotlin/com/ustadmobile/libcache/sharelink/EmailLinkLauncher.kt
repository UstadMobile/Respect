package com.ustadmobile.libcache.sharelink

interface EmailLinkLauncher {
    suspend fun launch(inviteLink: String)

    companion object {
        const val SCHEME = "mailto:"
    }
}
