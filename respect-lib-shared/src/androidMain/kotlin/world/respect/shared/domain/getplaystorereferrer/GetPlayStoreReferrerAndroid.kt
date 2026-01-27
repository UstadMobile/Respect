package world.respect.shared.domain.getplaystorereferrer

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.russhwolf.settings.Settings


class GetPlayStoreReferrerAndroid(
    private val context: Context,
    private val settings: Settings
) : GetPlayStoreReferrer {

    override fun fetchOnce() {
        if (settings.getBoolean(FETCHED, false)) {
            return
        }

        val client = InstallReferrerClient
            .newBuilder(context)
            .build()

        client.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(code: Int) {
                try {
                    if (code == InstallReferrerClient.InstallReferrerResponse.OK) {
                        val referrerUrl = client.installReferrer.installReferrer

                        settings.putString(REFERRER_URL, referrerUrl)
                    }
                } finally {
                    settings.putBoolean(FETCHED, true)
                    client.endConnection()
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
            }
        })
    }

    companion object {

        const val FETCHED = "install_referrer_fetched"
        const val REFERRER_URL = "install_referrer_url"
    }
}