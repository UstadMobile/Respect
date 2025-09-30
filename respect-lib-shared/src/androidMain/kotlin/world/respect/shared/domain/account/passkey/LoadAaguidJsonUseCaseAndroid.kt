package world.respect.shared.domain.account.passkey

import android.content.Context

class LoadAaguidJsonUseCaseAndroid(
    private val appContext: Context
) : LoadAaguidJsonUseCase{
    override fun invoke(): String ?{
        return appContext.assets.open("aaguid.json").bufferedReader(Charsets.UTF_8).use { it.readText() }

    }

}