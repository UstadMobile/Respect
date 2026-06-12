package world.respect.xapi.ipc.server

import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import world.respect.xapi.ipc.client.MessageRequestSenderBinderImpl
import world.respect.xapi.ipc.client.XapiResourceIpcClient
import kotlin.test.assertNotNull

class XapiServiceIntegrationTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

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

        class IncomingHandler(
            looper: Looper,
            val completeable: CompletableDeferred<Message>,
        ): Handler(looper) {
            override fun handleMessage(msg: Message) {
                completeable.complete(msg)
            }
        }

        val binder: IBinder = serviceRule.bindService(serviceIntent)
        assertNotNull(binder)
        val serviceMessenger = Messenger(binder)
        val client = XapiResourceIpcClient(
            MessageRequestSenderBinderImpl(serviceMessenger)
        )

        runBlocking {
            val response = client.statements.post(emptyList())
            println(response)
        }
    }


}