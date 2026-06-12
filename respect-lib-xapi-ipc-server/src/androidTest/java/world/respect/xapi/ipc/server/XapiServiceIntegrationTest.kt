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
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds

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
        val completeable = CompletableDeferred<Message>()
        val incomingHandler = IncomingHandler(Looper.getMainLooper(), completeable)
        val incomingMessenger = Messenger(incomingHandler)


        val msg: Message = Message.obtain(null, XapiIpcWhatFlags.WHAT_GET_STATEMENTS, 0, 0)
        msg.replyTo = incomingMessenger
        serviceMessenger.send(msg)

        val reply = runBlocking {
            withTimeout(10_000.milliseconds) {
                completeable.await()
            }
        }

        assertNotNull(reply)
    }


}