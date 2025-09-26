package world.respect.credentials.passkey

import android.os.Build

class CheckPasskeySupportUseCaseAndroidImpl(
    private val verifyDomainUseCase: VerifyDomainUseCase,
) : CheckPasskeySupportUseCase {

    override suspend fun invoke(rpId: String): Boolean {
        if (Build.VERSION.SDK_INT < 28)
            return false

        return verifyDomainUseCase(rpId)
    }

}