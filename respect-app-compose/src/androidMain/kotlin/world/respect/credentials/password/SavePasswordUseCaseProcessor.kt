package world.respect.credentials.password

import android.app.Activity
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialNoCreateOptionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class SavePasswordUseCaseProcessor(
    private val activityContext: Activity,
    private val jobChannel: Channel<CreatePasswordRequest>,
    private val processOnScope: CoroutineScope
) {

    private val credentialManager = CredentialManager.create(activityContext)
    suspend fun receiveJobs() {
        for (request in jobChannel) {
            processOnScope.launch {
                try {
                    credentialManager.createCredential(
                        context = activityContext,
                        request = request
                    )
                    print("Password saved")
                } catch (e: CreateCredentialNoCreateOptionException) {
                    print("No option to create credentials ${e.message}")
                } catch (e: CreateCredentialException) {
                    print("Error saving credentials ${e.message}")
                } catch (t: Throwable) {
                    print("Unexpected error saving credentials ${t.message}")
                }
            }
        }
    }
}