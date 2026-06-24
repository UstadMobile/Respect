package world.respect.xapi.ipc.client

import android.content.Context
import android.content.Intent
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.resources.XapiResource
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent

class XapiIpcClientBuilder(
    private val context: Context,
    private val endpointUrl: String,
) {

    private var json: Json? = null

    private var ipcPackageServiceName: String? = null

    private var auth: String? = null

    fun setJson(json : Json) : XapiIpcClientBuilder{
        this.json = json
        return this
    }

    fun setIpcServicePackageName(
        ipcPackageServiceName: String
    ): XapiIpcClientBuilder {
        this.ipcPackageServiceName = ipcPackageServiceName
        return this
    }

    fun setAuth(auth: String): XapiIpcClientBuilder {
        this.auth = auth
        return this
    }


    fun build(): XapiResource {
        return XapiResourceIpcClient(
            requestSender =XapiIpcMessageBridgeServiceConnectionImpl(
                context = context,
                intent = Intent(XapiIpcIntent.ACTION_XAPI_OVER_IPC).also {
                    it.`package` = ipcPackageServiceName
                        ?: throw IllegalArgumentException("Ipc service package not set")
                }
            ),
            json = json ?: Json { encodeDefaults = false },
            endpoint = Url(endpointUrl),
            auth = auth ?: throw IllegalArgumentException("No auth provided")
        )
    }

}