package world.respect.xapi.ipc.server

import android.content.Intent
import android.os.IBinder
import android.os.Messenger
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import io.ktor.http.Url
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.test.res.xapiSampleStatements
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.xapi.ipc.client.XapiMessageBridgeMessengerImpl
import world.respect.xapi.ipc.client.XapiResourceIpcClient
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

class XapiServiceIntegrationTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    val json = Json {
        encodeDefaults = false
    }

    private lateinit var ipcTestApplication: IpcTestApplication

    private lateinit var client: XapiResourceIpcClient

    @Before
    fun setup() {
        ipcTestApplication = ApplicationProvider.getApplicationContext()

        runBlocking {
            ipcTestApplication.insertAdminAndDefaultGrants()
        }

        val serviceIntent = Intent(
            ipcTestApplication,
            XapiMessengerService::class.java,
        )

        val binder: IBinder = serviceRule.bindService(serviceIntent)
        assertNotNull(binder)
        val serviceMessenger = Messenger(binder)

        client = XapiResourceIpcClient(
            XapiMessageBridgeMessengerImpl(serviceMessenger),
            json,
            Url("http://localhost/"),
            "secret",
        )
    }

    @Test
    fun givenServiceBound_whenRequestSent_thenResponseReceived() {
        val statement: XapiStatement = xapiSampleStatements(ipcTestApplication).first().let {
            json.decodeFromJsonElement(it.jsonObject)
        }

        runBlocking {
            val response = client.statements.post(listOf(statement))
            val ipcResponse = client.statements.get(
                listParams = XapiStatementsResource.GetStatementParams(
                    statementId = response.dataOrNull()!!.first()
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

    @Test
    fun givenServiceBound_whenReceiveAsFlowInvoked_thenStatementIsEmitted() {
        val stmtId = Uuid.random()
        val statement: XapiStatement = xapiSampleStatements(ipcTestApplication).first().let {
            json.decodeFromJsonElement(it.jsonObject)
        }

        runBlocking {
            val completeable = CompletableDeferred<XapiStatement>()

            val getJob = launch {
                client.statements.getAsFlow(
                    listParams = XapiStatementsResource.GetStatementParams(
                        statementId = stmtId
                    ),
                    dataLoadParams = DataLoadParams()
                ).collect {
                    val stmt = it.dataOrNull()?.statements?.firstOrNull()
                    if(stmt?.id == stmtId) {
                        completeable.complete(stmt)
                    }
                }
            }

            client.statements.post(listOf(statement.copy(id = stmtId)))

            val stmtReceived = completeable.await()
            assertNotNull(stmtReceived)
            getJob.cancel()
        }
    }


}