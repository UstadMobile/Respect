package world.respect.xapi.ipc.server

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceFlags
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags
import world.respect.xapi.ipc.shared.messages.ext.getDeserialized
import world.respect.xapi.ipc.shared.messages.ext.getStringValues
import world.respect.xapi.ipc.shared.messages.ext.putSerialized
import world.respect.xapi.ipc.shared.messages.ext.toBundle
import kotlin.uuid.Uuid

/**
 * Messenger service (server) that will receive xAPI requests, send them to a given xAPI resource,
 * and then send a reply message with the response.
 *
 * The replyTo field of the incoming message MUST be a Messenger of the client where responses
 * should be sent.
 *
 * See
 * https://developer.android.com/develop/background-work/services/bound-services#Messenger
 * https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/app/MessengerService.java
 *
 * Clients will register: then registered clients will receive invalidation messages, which can be
 * easily observed by the datasource on the other side.
 */
class XapiMessengerService: Service() {

    private val json = Json {
        encodeDefaults = false
    }

    internal class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext,
        private val json: Json,
    ):  Handler(Looper.getMainLooper()) {


        override fun handleMessage(msg: Message) {
            if(msg.what != XapiIpcWhatFlags.WHAT_REQUEST) {
                super.handleMessage(msg)
                return
            }

            val xapiResourceProvider = applicationContext as? XapiResourceProvider
                ?: throw IllegalStateException("No xapi resource provider")

            val endpoint = msg.data.getString(XapiIpcKeys.KEY_ENDPOINT)
                ?: throw IllegalArgumentException("Message has no endpoint")
            val auth = msg.data.getString(XapiIpcKeys.KEY_AUTH)
                ?: throw IllegalArgumentException("Message has no auth")

            val xapiResource = xapiResourceProvider.provideXapiResource(endpoint, auth)

            runBlocking {
                val replyMessage = Message.obtain(
                    this@IncomingHandler, XapiIpcWhatFlags.WHAT_RESPONSE
                )

                //Mark it as a response to the request id received.
                replyMessage.arg1 = msg.arg1

                when (msg.arg2) {
                    XapiIpcResourceFlags.POST_STATEMENTS -> {
                        //get the params and make the request

                        val uuids = xapiResource.statements.post(
                            list = msg.data.getDeserialized(
                                key = XapiIpcKeys.KEY_BODY,
                                json = json,
                                deserializer = ListSerializer(XapiStatement.serializer()),
                            ) ?: throw IllegalArgumentException()
                        )

                        replyMessage.data.putSerialized(
                            key = XapiIpcKeys.KEY_BODY,
                            json = json,
                            serializer = ListSerializer(Uuid.serializer()),
                            value = uuids
                        )

                        msg.replyTo.send(replyMessage)
                    }

                    XapiIpcResourceFlags.GET_STATEMENTS -> {
                        replyMessage.data = xapiResource.statements.get(
                            listParams = XapiStatementsResource.GetStatementParams.fromParams(
                                params = msg.data.getStringValues(
                                    XapiIpcKeys.KEY_QUERY_PARAMS
                                ) ?: Parameters.Empty,
                                json = json
                            )
                        ).toBundle(XapiStatementResult.serializer(), json)

                        msg.replyTo.send(replyMessage)
                    }

                    else -> {
                        super.handleMessage(msg)
                        null
                    }
                }
            }
        }
    }

    private val messenger: Messenger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Messenger(
            IncomingHandler(this, this.applicationContext, json)
        )
    }

    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }

    companion object {


    }

}