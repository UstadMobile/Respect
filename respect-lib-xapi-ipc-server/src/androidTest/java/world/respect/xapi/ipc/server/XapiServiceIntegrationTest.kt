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
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.test.res.xapiSampleStatements
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.xapi.ipc.client.XapiMessageBridgeBinderImpl
import world.respect.xapi.ipc.client.XapiResourceIpcClient
import kotlin.test.assertEquals
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
            XapiMessageBridgeBinderImpl(serviceMessenger),
            json,
            Url("http://localhost/"),
            "secret",
        )

        val statement: XapiStatement = xapiSampleStatements(ipcTestApplication).first().let {
            json.decodeFromJsonElement(it.jsonObject)
        }

        runBlocking {
            val response = client.statements.post(listOf(statement))
            val ipcResponse = client.statements.get(
                listParams = XapiStatementsResource.GetStatementParams(
                    statementId = response.first()
                )
            )

            val responseData = ipcResponse.dataOrNull()
            assertNotNull(responseData)
            assertEquals(
                statement.objectActivityOrNull()?.id ,
                responseData.statements.first().objectActivityOrNull()?.id
            )

            println(response)
        }
    }


}