package world.respect.credentials.passkey

import android.content.Context
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.os.Build

// to check domain is verified
// https://developer.android.com/training/app-links/verify-android-applinks#user-prompt-domain-verification-manager
//
class VerifyDomainUseCaseImpl(
    private val context: Context
) : VerifyDomainUseCase {

    override suspend fun invoke(rpId: String): Boolean {
        return if(Build.VERSION.SDK_INT >= 31) {
            val manager = context.getSystemService(DomainVerificationManager::class.java)


            val userState = manager.getDomainVerificationUserState(context.packageName)
                ?: return false

            val verifiedDomains = userState.hostToStateMap
                .filterValues { it == DomainVerificationUserState.DOMAIN_STATE_VERIFIED }

            verifiedDomains.keys.any { domain ->
                rpId.contains(domain)
            }
        }else {
            //This should look at the manifest. It will not be possible to check the verification
            //state, but we can make sure that the domain is one mentioned in the manifest.
            true
        }
    }

}
