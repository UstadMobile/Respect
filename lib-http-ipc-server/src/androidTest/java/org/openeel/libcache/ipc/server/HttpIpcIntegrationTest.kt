package org.openeel.libcache.ipc.server

import android.content.Intent
import android.os.IBinder
import android.os.Messenger
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import mockwebserver3.MockWebServer
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

    private lateinit var mockWebServer: MockWebServer

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

        mockWebServer = MockWebServer()
    }

    @After
    fun teardown() {
        mockWebServer.close()
        ipcHttpClient.close()
    }

    private fun assertResponseMatches(
        path: String
    ) {
        val bodyBytes = ipcTestApplication.assets.open(path).use {
            it.readAllBytes()
        }

        mockWebServer.dispatcher = MockWebServerAssetDispatcher(ipcTestApplication)
        mockWebServer.start()

        val response = ipcHttpClient.newCall(
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

        val response = ipcHttpClient.newCall(
            Request.Builder()
                .url(mockWebServer.url("/doesnotexist"))
                .build()
        ).execute()

        assertEquals(404, response.code)
    }

    @Test
    fun givenMockServerNotRunning_whenRequestMade_thenReceivesGatewayError() {
        val response = ipcHttpClient.newCall(
            Request.Builder()
                .url("http://localhost:8080/servernotrunning")
                .build()
        ).execute()

        assertEquals(502, response.code)
    }


}