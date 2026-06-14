package world.respect.xapi.ipc.server

import android.content.Intent
import android.os.IBinder
import android.os.Messenger
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.junit.Rule
import org.junit.Test
import world.respect.lib.test.res.xapiSampleStatements
import world.respect.lib.xapi.model.XapiStatement
import world.respect.xapi.ipc.client.MessageRequestSenderBinderImpl
import world.respect.xapi.ipc.client.XapiResourceIpcClient
import kotlin.test.assertNotNull

class XapiServiceIntegrationTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    val json = Json {
        encodeDefaults = false
    }

    @Test
    fun givenValidService_whenRequestSent_thenResponseReceived() {
        val ipcTestApplication = ApplicationProvider.getApplicationContext<IpcTestApplication>()
        val serviceIntent = Intent(
            ipcTestApplication,
            XapiMessengerService::class.java,
        )

        runBlocking {
            ipcTestApplication.insertAdminAndDefaultGrants()
        }

        val binder: IBinder = serviceRule.bindService(serviceIntent)
        assertNotNull(binder)
        val serviceMessenger = Messenger(binder)
        val client = XapiResourceIpcClient(
            MessageRequestSenderBinderImpl(serviceMessenger),
            json,
            Url("http://localhost/"),
            "secret",
        )

        val statement: XapiStatement = xapiSampleStatements(ipcTestApplication).first().let {
            json.decodeFromJsonElement(it.jsonObject)
        }

        runBlocking {
            xapiSampleStatements(ipcTestApplication).first().jsonObject
            val response = client.statements.post(listOf(statement))
            println(response)
        }
    }


}