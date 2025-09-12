package world.respect.credentials.passkey

import android.content.Context
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.os.Build
import androidx.annotation.RequiresApi

// to check domain is verified
// https://developer.android.com/training/ app-links/verify-android-applinks#user-prompt-domain-verification-manager
//
class VerifyDomainUseCaseImpl(
    private val context: Context
) : VerifyDomainUseCase {

    @RequiresApi(Build.VERSION_CODES.S)
    override suspend fun invoke(rpId: String): Boolean {
        val manager = context.getSystemService(DomainVerificationManager::class.java)


        val userState = manager.getDomainVerificationUserState(context.packageName)
            ?: return false

        val verifiedDomains = userState.hostToStateMap
            .filterValues { it == DomainVerificationUserState.DOMAIN_STATE_VERIFIED }


        return verifiedDomains.keys.any { domain ->
            rpId.contains(domain)
        }
    }
}
