package world.respect.credentials.passkey

import android.util.Log
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.credentials.passkey.request.CreatePublicKeyCredentialCreationOptionsJsonUseCase
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create a passkey on Android. This will show a bottom sheet for the user to approve creating a new
 * passkey.
 *
 * The App's DI is at the Application context level, however passkeys require an activity context.
 * This UseCase provides a channel that must be collected by the activity (eg MainActivity) using
 * CreatePasskeyUseCaseProcessor.
 *
 * See https://developer.android.com/identity/sign-in/credential-manager#create-passkey
 */
class CreatePasskeyUseCaseAndroidImpl(
    private val json: Json,
    private val createPublicKeyJsonUseCase: CreatePublicKeyCredentialCreationOptionsJsonUseCase
) : CreatePasskeyUseCase {

    data class CreatePublicKeyCredentialRequestJob(
        val request: CreatePublicKeyCredentialRequest,
        val id: Int,
        val response: CompletableDeferred<CreatePublicKeyCredentialResponse> = CompletableDeferred()
    )

    private val idCounter = AtomicInteger()

    val requestChannel = Channel<CreatePublicKeyCredentialRequestJob>(capacity = Channel.UNLIMITED)

    /**
     * @throws CreateCredentialException if CredentialManager throws an exception
     */
    override suspend fun invoke(
        username: String,
        rpId: String
    ): CreatePasskeyUseCase.CreatePasskeyResult {
        return try {
            val job = CreatePublicKeyCredentialRequestJob(
                request = CreatePublicKeyCredentialRequest(
                    requestJson = json.encodeToString(
                        createPublicKeyJsonUseCase(username, rpId)
                    ),
                    preferImmediatelyAvailableCredentials = false,
                ),
                id = idCounter.incrementAndGet(),
            )
            requestChannel.trySend(job)
            val response = job.response.await()

            Log.d ( "passkey response:", response.registrationResponseJson)
            val passkeyResponse = json.decodeFromString<AuthenticationResponseJSON>(
                response.registrationResponseJson
            )

            CreatePasskeyUseCase.PasskeyCreatedResult(passkeyResponse)
        } catch (_: CreateCredentialCancellationException) {
            CreatePasskeyUseCase.UserCanceledResult()
        } catch (e: CreateCredentialException) {
            // See https://codelabs.developers.google.com/credential-manager-api-for-android#1
            Log.e(
                 "CreatePassKeyUseCase",  e.message, e,
            )
            CreatePasskeyUseCase.Error(e.message)
        }
    }
}