package world.respect

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import world.respect.shared.domain.account.authwithopenid.OpenIdAuthorizationResult
import world.respect.shared.domain.account.authwithopenid.OpenIdAuthorizationUseCase
import world.respect.shared.domain.activitycontextjobprocessor.EnqueueActivityContextJobUseCase
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OpenIdAuthorizationUseCaseAndroid(
    private val enqueueActivityContextJobUseCase: EnqueueActivityContextJobUseCase,
) : OpenIdAuthorizationUseCase {

    private val pendingResults = ConcurrentHashMap<
        String,
        CompletableDeferred<OpenIdAuthorizationResult>,
    >()

    override suspend operator fun invoke(issuer: Url): OpenIdAuthorizationResult {
        val requestId = UUID.randomUUID().toString()
        val result = CompletableDeferred<OpenIdAuthorizationResult>()
        pendingResults[requestId] = result

        try {
            val serviceConfiguration = fetchServiceConfiguration(issuer)
            val authorizationRequest = AuthorizationRequest.Builder(
                serviceConfiguration,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                REDIRECT_URI.toUri(),
            )
                .setScope("openid")
                .build()

            enqueueActivityContextJobUseCase { activity ->
                startAuthorization(
                    activity = activity,
                    requestId = requestId,
                    issuer = issuer,
                    authorizationRequest = authorizationRequest,
                )
            }

            return result.await()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            return errorResult(requestId, issuer, e.message ?: "Unable to start OpenID login")
        } finally {
            pendingResults.remove(requestId)
        }
    }

    private suspend fun fetchServiceConfiguration(
        issuer: Url,
    ): AuthorizationServiceConfiguration = suspendCancellableCoroutine { continuation ->
        AuthorizationServiceConfiguration.fetchFromIssuer(
            issuer.toString().toUri(),
            { serviceConfiguration, exception ->
                if (serviceConfiguration != null) {
                    continuation.resume(serviceConfiguration)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException(
                            exception?.message ?: "Unable to load OpenID configuration"
                        )
                    )
                }
            },
        )
    }

    private fun startAuthorization(
        activity: Activity,
        requestId: String,
        issuer: Url,
        authorizationRequest: AuthorizationRequest,
    ) {
        val authorizationService = AuthorizationService(
            activity,
            appAuthConfiguration(issuer),
        )

        try {
            val callback = resultPendingIntent(activity, requestId, issuer)
            authorizationService.performAuthorizationRequest(
                authorizationRequest,
                callback,
                callback,
            )
        } catch (e: Exception) {
            publishResult(
                errorResult(requestId, issuer, e.message ?: "Unable to start OpenID login")
            )
        } finally {
            authorizationService.dispose()
        }
    }

    private fun resultPendingIntent(
        activity: Activity,
        requestId: String,
        issuer: Url,
    ): PendingIntent {
        val requestCode = requestId.hashCode()
        val callbackIntent = Intent(activity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_REQUEST_ID, requestId)
            putExtra(EXTRA_ISSUER, issuer.toString())
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )

        return PendingIntent.getActivity(
            activity,
            requestCode,
            callbackIntent,
            flags,
        )
    }

    fun handleAuthorizationResult(
        activity: Activity,
        intent: Intent,
    ) {
        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID) ?: return
        val issuer = intent.getStringExtra(EXTRA_ISSUER)?.let(::Url) ?: return

        val authorizationException = AuthorizationException.fromIntent(intent)
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)
        if (authorizationResponse == null) {
            publishResult(
                errorResult(
                    requestId,
                    issuer,
                    authorizationException?.message ?: "OpenID login failed",
                )
            )
            return
        }

        val authorizationService = AuthorizationService(
            activity.applicationContext,
            appAuthConfiguration(issuer),
        )
        try {
            authorizationService.performTokenRequest(
                authorizationResponse.createTokenExchangeRequest()
            ) { tokenResponse, exception ->
                publishResult(
                    OpenIdAuthorizationResult(
                        requestId = requestId,
                        issuer = issuer,
                        authorizationCode = authorizationResponse.authorizationCode,
                        accessToken = tokenResponse?.accessToken,
                        idToken = tokenResponse?.idToken,
                        errorMessage = exception?.message,
                    )
                )
                authorizationService.dispose()
            }
        } catch (e: Exception) {
            authorizationService.dispose()
            publishResult(
                errorResult(
                    requestId,
                    issuer,
                    e.message ?: "OpenID token exchange failed",
                    authorizationCode = authorizationResponse.authorizationCode,
                )
            )
        }
    }

    private fun errorResult(
        requestId: String,
        issuer: Url,
        message: String,
        authorizationCode: String? = null,
    ) = OpenIdAuthorizationResult(
        requestId = requestId,
        issuer = issuer,
        authorizationCode = authorizationCode,
        errorMessage = message,
    )

    private fun publishResult(result: OpenIdAuthorizationResult) {
        pendingResults.remove(result.requestId)?.complete(result)
    }

    private fun appAuthConfiguration(issuer: Url): AppAuthConfiguration =
        AppAuthConfiguration.Builder()
            .setSkipIssuerHttpsCheck(issuer.toString().startsWith("http://"))
            .build()

    companion object {
        const val EXTRA_REQUEST_ID = "openid_request_id"
        const val EXTRA_ISSUER = "openid_issuer"
        const val CLIENT_ID = "respect-android"
        /* As per https://github.com/openid/AppAuth-Android#capturing-the-authorization-redirect
          we need to set uri in client setting in Valid redirect URIs:
          world.respect.oauth:/oauth2redirect
          otherwise we will get error invalid parameter indirect_uri
        */
        const val REDIRECT_URI = "world.respect.oauth:/oauth2redirect"
    }
}
