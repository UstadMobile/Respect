package world.respect.xapi.ipc.client

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.resources.XapiActivitiesResource
import world.respect.lib.xapi.resources.XapiAgentsResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStatementsResource

/**
 * XapiResourceIpcClient must host an interface implementation for messages for which a reply is
 * expected e.g.
 *
 * returns response
 * suspend fun sendRequest(request: Message): Message
 *  .. sends message using the messenger, waits for reply by using a deferred completable.
 *
 */
class XapiResourceIpcClient(
    private val requestSender: XapiMessageBridge,
    private val json: Json,
    private val endpoint: Url,
    private val auth: String,
): XapiResource {
    override val statements: XapiStatementsResource by lazy {
        XapiStatementsResourceIpcClient(requestSender, json, endpoint, auth)
    }

    override val agents: XapiAgentsResource
        get() = TODO("Not yet implemented")

    override val activities: XapiActivitiesResource
        get() = TODO("Not yet implemented")

    override fun close() {
        requestSender.close()
    }

}