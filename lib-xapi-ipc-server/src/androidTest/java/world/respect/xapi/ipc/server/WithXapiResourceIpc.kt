package world.respect.xapi.ipc.server

import android.content.Intent
import android.os.IBinder
import android.os.Messenger
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeMessengerImpl
import world.respect.lib.xapi.resources.XapiResource
import world.respect.xapi.ipc.client.XapiResourceIpcClient
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent
import kotlin.test.assertNotNull

/**
 * Executes a test block with a bound [XapiResource] IPC client and clean database state.
 */
suspend fun withXapiResourceIpcTest(
    serviceRule: ServiceTestRule,
    json: Json,
    block: suspend (XapiResource) -> Unit
) {
    val ipcTestApplication = ApplicationProvider.getApplicationContext<IpcTestApplication>()

    val intent = Intent(XapiIpcIntent.ACTION_XAPI_OVER_IPC)
    intent.`package` = ipcTestApplication.packageName

    val binder: IBinder? = serviceRule.bindService(intent)
    assertNotNull(binder)
    val serviceMessenger = Messenger(binder)

    val client = XapiResourceIpcClient(
        requestSender = IpcMessageBridgeMessengerImpl(serviceMessenger),
        json = json,
        endpoint = Url("http://localhost/"),
        auth = "secret",
        clientPackageName = ipcTestApplication.packageName
    )

    ipcTestApplication.schoolDatabase.clearAllTables()
    ipcTestApplication.insertAdminAndDefaultGrants()

    try {
        block(client)
    } finally {
        client.close()
    }
}