package world.respect

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.connectivity.ConnectionBuilder
import java.net.HttpURLConnection
import java.net.URL

class KeycloakTestActivity : ComponentActivity() {

    private lateinit var authService: AuthorizationService

    private val status = mutableStateOf("")

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
            status.value = "Cancelled"
            return@registerForActivityResult
        }

        val resp = AuthorizationResponse.fromIntent(data)
        val err = AuthorizationException.fromIntent(data)
        if (resp == null) {
            status.value = err?.message.toString()
            return@registerForActivityResult
        }

        authService.performTokenRequest(resp.createTokenExchangeRequest()) { token, ex ->
            status.value = if (token?.accessToken != null) {
                "${token.accessToken}"
            } else {
                "${ex?.message}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dev-only: allow plain HTTP so token requests to a local Keycloak (http://...) work.
        val appAuthConfig = AppAuthConfiguration.Builder()
            .setConnectionBuilder(InsecureConnectionBuilder)
            .build()
        authService = AuthorizationService(this, appAuthConfig)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(status.value) { url, realm, clientId ->
                        login(url, realm, clientId)
                    }
                }
            }
        }
    }

    private fun login(serverUrl: String, realm: String, clientId: String) {
        status.value = ""
        val config = AuthorizationServiceConfiguration(
            Uri.parse("$serverUrl/realms/$realm/protocol/openid-connect/auth"),
            Uri.parse("$serverUrl/realms/$realm/protocol/openid-connect/token")
        )

        val request = AuthorizationRequest.Builder(
            config,
            clientId,
            ResponseTypeValues.CODE,
            Uri.parse("world.respect.oauth:/oauth2redirect")
        ).setScope("openid profile email").build()

        loginLauncher.launch(authService.getAuthorizationRequestIntent(request))
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }
}

private object InsecureConnectionBuilder : ConnectionBuilder {
    override fun openConnection(uri: Uri): HttpURLConnection {
        return URL(uri.toString()).openConnection() as HttpURLConnection
    }
}

@Composable
private fun LoginScreen(
    status: String,
    onLogin: (url: String, realm: String, clientId: String) -> Unit
) {
    var url by remember { mutableStateOf("http://192.168.1.44:8080") }
    val realm ="myrealm"
    val clientId ="respect-android"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("url") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onLogin(url, realm, clientId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        if (status.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(status)
        }
    }
}
