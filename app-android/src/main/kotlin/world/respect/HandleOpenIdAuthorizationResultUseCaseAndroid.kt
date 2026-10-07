package world.respect

import android.content.Context
import android.content.Intent
import io.ktor.http.Url
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenResponse
import world.respect.shared.domain.account.authwithopenid.VerifyOpenIdTokenUseCase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HandleOpenIdAuthorizationResultUseCaseAndroid(
    context: Context,
    private val verifyOpenIdTokenUseCase: VerifyOpenIdTokenUseCase,
) {

    private val appContext = context.applicationContext

    sealed interface Result {
        data object NotAuthorizationCallback : Result
        data object Canceled : Result
        data class Verified(
            val issuer: Url,
            val schoolUrl: Url,
            val accessToken: String,
            val idToken: String?,
        ) : Result
    }

    suspend operator fun invoke(intent: Intent): Result {
        when (intent.action) {
            OpenIdAuthorizationUseCaseAndroid.ACTION_OPENID_AUTHORIZATION_CANCELED -> {
                return Result.Canceled
            }

            OpenIdAuthorizationUseCaseAndroid.ACTION_OPENID_AUTHORIZATION_RESULT -> Unit
            else -> return Result.NotAuthorizationCallback
        }

        val issuerText = intent.getStringExtra(OpenIdAuthorizationUseCaseAndroid.EXTRA_ISSUER)
        val schoolText = intent.getStringExtra(OpenIdAuthorizationUseCaseAndroid.EXTRA_SCHOOL_URL)
        if (issuerText == null || schoolText == null) {
            throw IllegalArgumentException("OpenID callback is missing its issuer or school URL")
        }

        val issuer = Url(issuerText)
        val schoolUrl = Url(schoolText)
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)
        if (authorizationResponse == null) {
            val authorizationException = AuthorizationException.fromIntent(intent)
            throw IllegalStateException(
                authorizationException?.message ?: "OpenID authorization returned no response"
            )
        }

        val authorizationService = AuthorizationService(
            appContext,
            openIdAppAuthConfiguration(issuer = issuer),
        )
        val tokenResponse = try {
            suspendCancellableCoroutine<TokenResponse> { continuation ->
                continuation.invokeOnCancellation {
                    authorizationService.dispose()
                }

                val tokenRequest = authorizationResponse.createTokenExchangeRequest()
                authorizationService.performTokenRequest(tokenRequest) { response, exception ->
                    authorizationService.dispose()
                    if (continuation.isActive) {
                        if (response?.accessToken != null) {
                            continuation.resume(value = response)
                        } else {
                            continuation.resumeWithException(
                                exception = IllegalStateException(
                                    exception?.message
                                        ?: "OpenID token exchange returned no access token"
                                )
                            )
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            authorizationService.dispose()
            throw exception
        }

        val accessToken = tokenResponse.accessToken
            ?: throw IllegalStateException("OpenID token exchange returned no access token")
        if (!verifyOpenIdTokenUseCase(schoolUrl = schoolUrl, accessToken = accessToken)) {
            throw IllegalStateException("School server rejected the OpenID token")
        }

        return Result.Verified(
            issuer = issuer,
            schoolUrl = schoolUrl,
            accessToken = accessToken,
            idToken = tokenResponse.idToken,
        )
    }
}
