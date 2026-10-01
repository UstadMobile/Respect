package world.respect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.connectivity.ConnectionBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import world.respect.shared.domain.account.authwithopenid.OpenIdAuthorizationResult
import world.respect.shared.domain.account.authwithopenid.OpenIdAuthorizationUseCase
import java.net.HttpURLConnection
import java.net.URL

class OpenIdAuthorizationActivity : ComponentActivity(), KoinComponent {

    private val openIdAuthorizationUseCase: OpenIdAuthorizationUseCase by inject()

    private lateinit var authorizationService: AuthorizationService

    private val authorizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleAuthorizationResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID)
        val issuer = intent.getStringExtra(EXTRA_ISSUER)

        if (requestId == null || issuer == null) {
            finish()
            return
        }

        authorizationService = AuthorizationService(
            this,
            AppAuthConfiguration.Builder()
                .setConnectionBuilder(OpenIdConnectionBuilder)
                .setSkipIssuerHttpsCheck(issuer.startsWith("http://"))
                .build(),
        )

        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse(issuer),
            { serviceConfiguration, exception ->
                if (serviceConfiguration == null) {

                    publishResult(
                        requestId = requestId,
                        issuer = issuer,
                        errorMessage = exception?.message ?: "Unable to load OpenID configuration",
                    )
                } else {
                    val authorizationRequest = AuthorizationRequest.Builder(
                        serviceConfiguration,
                        CLIENT_ID,
                        ResponseTypeValues.CODE,
                        Uri.parse(REDIRECT_URI),
                    )
                        .setScope("openid")
                        .build()



                    authorizationLauncher.launch(
                        authorizationService.getAuthorizationRequestIntent(authorizationRequest)
                    )
                }
            },
            OpenIdConnectionBuilder,
        )
    }

    private fun handleAuthorizationResult(data: Intent?) {
        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID)
        val issuer = intent.getStringExtra(EXTRA_ISSUER)

        if (requestId == null || issuer == null) {
            finish()
            return
        }

        if (data == null) {
            publishResult(requestId, issuer, errorMessage = "OpenID login was cancelled")
            return
        }

        val authorizationResponse = AuthorizationResponse.fromIntent(data)
        val authorizationException = AuthorizationException.fromIntent(data)

        if (authorizationResponse == null) {
            publishResult(
                requestId = requestId,
                issuer = issuer,
                errorMessage = authorizationException?.message ?: "OpenID login failed",
            )
            return
        }

        authorizationService.performTokenRequest(
            authorizationResponse.createTokenExchangeRequest()
        ) { tokenResponse, exception ->

            publishResult(
                requestId = requestId,
                issuer = issuer,
                authorizationCode = authorizationResponse.authorizationCode,
                accessToken = tokenResponse?.accessToken,
                idToken = tokenResponse?.idToken,
                errorMessage = exception?.message,
            )
        }
    }

    private fun publishResult(
        requestId: String,
        issuer: String,
        authorizationCode: String? = null,
        accessToken: String? = null,
        idToken: String? = null,
        errorMessage: String? = null,
    ) {
        openIdAuthorizationUseCase.publishResult(
            OpenIdAuthorizationResult(
                requestId = requestId,
                issuer = Url(issuer),
                authorizationCode = authorizationCode,
                accessToken = accessToken,
                idToken = idToken,
                errorMessage = errorMessage,
            )
        )
        finish()
    }

    override fun onDestroy() {
        if (::authorizationService.isInitialized) {
            authorizationService.dispose()
        }
        super.onDestroy()
    }

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

private object OpenIdConnectionBuilder : ConnectionBuilder {

    override fun openConnection(uri: Uri): HttpURLConnection {
        return URL(uri.toString()).openConnection() as HttpURLConnection
    }
}
