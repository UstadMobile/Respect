package world.respect.credentials.passkey

import android.app.Activity
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import kotlinx.coroutines.channels.Channel

/**
 * Receive requests to create a passkey from the CreatePasskeyUseCase and process them using an
 * activity context.
 */
class CreatePasskeyUseCaseProcessor(
    private val activityContext: Activity,
    private val jobChannel: Channel<CreatePasskeyUseCaseAndroidImpl.CreatePublicKeyCredentialRequestJob>,
) {

    val credentialManager = CredentialManager.create(activityContext)

    suspend fun processJobs() {
        for (job in jobChannel) {
            try {
                val response = credentialManager.createCredential(
                    context = activityContext,
                    request = job.request
                ) as CreatePublicKeyCredentialResponse
                job.response.complete(response)
            }catch(e: Throwable) {
                job.response.completeExceptionally(e)
            }
        }
    }


}