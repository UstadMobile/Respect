package org.openeel.libcache.ipc.server

import android.content.Intent
import android.os.IBinder
import android.os.Messenger
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import mockwebserver3.MockWebServer
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import okhttp3.Request
import okio.use

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.openeel.libcache.ipc.client.IpcHttpClient
import org.openeel.libcache.ipc.client.IpcHttpClientImpl
import org.openeel.libcache.ipc.core.HttpIpcIntent

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class HttpIpcIntegrationTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    private lateinit var ipcTestApplication: IpcTestApplication

    private lateinit var ipcHttpClient: IpcHttpClient

    @Before
    fun setup() {
        ipcTestApplication = ApplicationProvider.getApplicationContext()

        val intent = Intent(HttpIpcIntent.ACTION_HTTP_OVER_IPC_CONNECT)
        intent.`package` = ipcTestApplication.packageName

        val binder: IBinder = serviceRule.bindService(intent)
        assertNotNull(binder)
        val messenger = Messenger(binder)

        ipcHttpClient = IpcHttpClientImpl(
            outgoingMessengerProvider = { messenger }
        )
    }

    @Test
    fun givenValidRequest_whenMadeViaIpcClient_thenResponseMatches() {
        val bodyStr = "Hello, world"

        val dispatcher : Dispatcher = object: Dispatcher() {
            override fun dispatch(request: mockwebserver3.RecordedRequest): MockResponse {
                return MockResponse.Builder()
                    .code(200)
                    .body(bodyStr)
                    .build()
            }
        }

        MockWebServer().use { mockWebServer ->
            mockWebServer.dispatcher = dispatcher
            mockWebServer.start()

            val response = ipcHttpClient.newCall(
                Request.Builder()
                    .url(mockWebServer.url("/"))
                    .build()
            ).execute()

            val bodyFromResponse = response.body.string()
            assertEquals(bodyStr, bodyFromResponse)
        }
    }
}