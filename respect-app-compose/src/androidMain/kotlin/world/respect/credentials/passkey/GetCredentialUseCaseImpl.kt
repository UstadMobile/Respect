package world.respect.credentials.passkey

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.credentials.passkey.request.CreatePublicKeyCredentialRequestOptionsJsonUseCase

class GetCredentialUseCaseImpl(
    private val context: Context,
    private val createPublicKeyCredentialRequestOptionsJsonUseCase: CreatePublicKeyCredentialRequestOptionsJsonUseCase,
    private val json: Json,
) : GetCredentialUseCase {

    override suspend fun invoke(rpId: String): GetCredentialUseCase.CredentialResult {
        val credentialManager = CredentialManager.create(context)

        val getPasswordOption = GetPasswordOption()
        val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
            requestJson = json.encodeToString(createPublicKeyCredentialRequestOptionsJsonUseCase(rpId=rpId))
        )

        //As per https://developer.android.com/identity/sign-in/credential-manager#sign-in when
        // preferImmediatelyAvailableCredentials = true then the dialog will only be shown if the
        // user has accounts that they can select.
        val getCredentialRequest = GetCredentialRequest(
            credentialOptions = listOf(getPasswordOption, getPublicKeyCredentialOption),
            preferImmediatelyAvailableCredentials = true
        )

        return try {
            val result = credentialManager.getCredential(
                context = context,
                request = getCredentialRequest
            )

            //As per https://developer.android.com/identity/sign-in/credential-manager#sign-in
            when (val credential = result.credential) {
                is PasswordCredential -> {
                    GetCredentialUseCase.PasswordCredentialResult(
                        credentialUsername = credential.id,
                        password = credential.password
                    )
                }

                is PublicKeyCredential -> {
                    val authResponseJson = credential.authenticationResponseJson
                    Log.d ("passkey response" , authResponseJson)
                    val parsedResponse = json.decodeFromString<AuthenticationResponseJSON>(authResponseJson)

                    GetCredentialUseCase.PasskeyCredentialResult(
                       parsedResponse
                    )
                }

                else -> {
                    GetCredentialUseCase.Error("Unknown credential type.")
                }
            }
        } catch (e: NoCredentialException) {
            GetCredentialUseCase.NoCredentialAvailableResult()
        }
        catch (e: GetCredentialCancellationException) {
            GetCredentialUseCase.UserCanceledResult()
        }catch (e: GetCredentialException) {
            GetCredentialUseCase.Error("Failed to get credential: ${e.message}")
        }
    }
}