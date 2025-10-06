package world.respect.credentials.password

import androidx.credentials.CreatePasswordRequest
import kotlinx.coroutines.channels.Channel
import world.respect.credentials.passkey.password.SavePasswordUseCase

class SavePasswordUseCaseAndroidImpl : SavePasswordUseCase {

    val requestChannel = Channel<CreatePasswordRequest>(capacity = Channel.RENDEZVOUS)

    override suspend fun invoke(username: String, password: String) {
        val request = CreatePasswordRequest(
            id = username,
            password = password
        )

       requestChannel.trySend(request)

    }
}
