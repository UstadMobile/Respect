package org.openeel.libcache.ipc.server

import android.content.Intent
import android.os.IBinder
import android.os.Messenger
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketEffect
import okhttp3.Request
import okio.use
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.openeel.libcache.ipc.client.HttpIpcClient
import org.openeel.libcache.ipc.client.HttpIpcClientImpl
import org.openeel.libcache.ipc.core.HttpIpcIntent
import java.util.concurrent.TimeUnit

/**
 * Test the HttpIpc service and client using a real web server (MockWebServer) to make real HTTP
 * requests with various scenarios.
 */
@RunWith(AndroidJUnit4::class)
class HttpIpcIntegrationTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    private lateinit var ipcTestApplication: IpcTestApplication

    private lateinit var httpIpcClient: HttpIpcClient

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        ipcTestApplication = ApplicationProvider.getApplicationContext()

        val intent = Intent(HttpIpcIntent.ACTION_HTTP_OVER_IPC_CONNECT)
        intent.`package` = ipcTestApplication.packageName

        val binder: IBinder = serviceRule.bindService(intent)
        assertNotNull(binder)
        val messenger = Messenger(binder)

        httpIpcClient = HttpIpcClientImpl(
            outgoingMessengerProvider = { messenger }
        )

        mockWebServer = MockWebServer()
    }

    @After
    fun teardown() {
        mockWebServer.close()
        httpIpcClient.close()
    }

    private fun assertResponseMatches(
        path: String
    ) {
        val bodyBytes = ipcTestApplication.assets.open(path).use {
            it.readAllBytes()
        }

        mockWebServer.dispatcher = MockWebServerAssetDispatcher(ipcTestApplication)
        mockWebServer.start()

        val response = httpIpcClient.newCall(
            Request.Builder()
                .url(mockWebServer.url("/$path"))
                .build()
        ).execute()

        val bodyFromResponse = response.body.source().readByteArray()
        Assert.assertArrayEquals(bodyBytes, bodyFromResponse)
    }


    @Test
    fun givenValidRequest_whenMadeViaIpcClient_thenResponseMatches() {
        assertResponseMatches("xapistatements/group-statement.json")
        assertResponseMatches("media/video.mp4")
    }

    @Test
    fun givenUriNotFound_whenRequestMade_thenReceives404Response() {
        mockWebServer.dispatcher = MockWebServerAssetDispatcher(ipcTestApplication)
        mockWebServer.start()

        val response = httpIpcClient.newCall(
            Request.Builder()
                .url(mockWebServer.url("/doesnotexist"))
                .build()
        ).execute()

        assertEquals(404, response.code)
    }

    @Test
    fun givenMockServerNotRunning_whenRequestMade_thenReceivesGatewayError() {
        val response = httpIpcClient.newCall(
            Request.Builder()
                .url("http://localhost:8080/servernotrunning")
                .build()
        ).execute()

        assertEquals(502, response.code)
    }

    @Test
    fun givenMockServerHeadersTimeOut_whenRequestMade_thenReceivesGatewayError() {
        mockWebServer.start()

        mockWebServer.enqueue(MockResponse.Builder()
            .headersDelay(50_000, TimeUnit.MILLISECONDS)
            .body("Hello World")
            .code(200)
            .build()
        )

        val call = httpIpcClient.newCall(
            Request.Builder()
                .url(mockWebServer.url("xapistatements/group-statement.json"))
                .build()
        )

        val timeout = call.timeout()
        val response = call.execute()

        assertEquals(502, response.code)

        assertEquals(
            IpcTestApplication.TIMEOUT_DURATION_SECS * 1_000_000_000,
            timeout.timeoutNanos()
        )
    }

    @Test
    fun givenServerDisconnects_whenRequestMade_thenReceivesGatewayError() {
        mockWebServer.start()
        val bodyContent = "Can't get this, hammer time"

        mockWebServer.enqueue(
            MockResponse.Builder()
                .body(bodyContent)
                .onResponseStart(SocketEffect.CloseSocket())
            .build()
        )

        val response = httpIpcClient.newCall(
            Request.Builder()
                .url(mockWebServer.url("/"))
                .build()
        ).execute()

        val bodyAsString = response.body.string()
        Assert.assertNotEquals(bodyContent, bodyAsString)
        assertEquals(502, response.code)
    }



}