package world.respect.xapi.ipc.server.ext

import android.os.Bundle
import android.os.Message
import android.util.Log
import io.ktor.http.Headers
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeWhatFlags
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.toPrettyString
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.ISingleDocumentParams
import world.respect.lib.xapi.resources.XapiDocumentResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.xapi.ipc.server.XapiIpcResourceIncomingHandler
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import world.respect.xapi.ipc.shared.messages.XapiIpcMethodEnum
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceAndMethod
import world.respect.xapi.ipc.shared.messages.XapiIpcTags
import world.respect.xapi.ipc.shared.messages.ext.getStringValues
import world.respect.xapi.ipc.shared.messages.ext.toBundle
import world.respect.xapi.ipc.shared.messages.ext.toXapiDocument
import java.util.concurrent.ExecutorService
import kotlin.collections.set

abstract class AbstractDocumentResourceIncomingHandler<
        MultiDocParams: Any,
        SingleDocParams: ISingleDocumentParams<MultiDocParams>,
        T: XapiDocumentResource<MultiDocParams, SingleDocParams>
>(
    private val xapiResourceProvider: XapiResourceProvider,
    protected val json: Json,
    private val scope: CoroutineScope,
    private val executor: ExecutorService,
) : XapiIpcResourceIncomingHandler {

    abstract fun XapiResource.documentResource(): T

    abstract fun Bundle.getMultiDocParams(): MultiDocParams

    abstract fun Bundle.getSingleDocParams(): SingleDocParams

    private val flowCollectors = ConcurrentMap<Int, Job>()

    override fun handleMessage(msg: Message) {
        val bundle = msg.data
        val replyTo = msg.replyTo
        val incomingMessageId = msg.arg1


        val xapiResource = runBlocking {
            xapiResourceProvider.provideResource(bundle)
        }

        val docResource = xapiResource.documentResource()

        val logPrefix = "XapiIpcService (client=) msg #$incomingMessageId)"

        val replyMessage = Message.obtain().also {
            it.what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE
            it.arg1 = incomingMessageId
        }

        when(XapiIpcResourceAndMethod.fromArg2Int(msg.arg2).method) {
            XapiIpcMethodEnum.GET -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: get state")
                val requestHeaders = msg.data.getStringValues(XapiIpcKeys.KEY_HEADERS)?.let {
                    Headers.build { appendAll(it) }
                } ?: Headers.Empty

                val dataLoadParams = DataLoadParams(requestHeaders = requestHeaders)
                replyMessage.data = runBlocking {
                    docResource.get(
                        params = bundle.getSingleDocParams(),
                        dataLoadParams = dataLoadParams,
                    ).also {
                        Log.d(XapiIpcTags.LOGTAG, "$logPrefix get state: response ${it.toPrettyString()}")
                    }.toBundle(executor)
                }

                msg.replyTo.send(replyMessage)
            }

            XapiIpcMethodEnum.GET_AS_FLOW -> {
                val requestHeaders = msg.data.getStringValues(XapiIpcKeys.KEY_HEADERS)?.let {
                    Headers.build { appendAll(it) }
                } ?: Headers.Empty
                val dataLoadParams = DataLoadParams(requestHeaders = requestHeaders)
                scope.launch {
                    Log.d(XapiIpcTags.LOGTAG, "$logPrefix #$incomingMessageId getAsFlow state")
                    docResource.getAsFlow(
                        params = bundle.getSingleDocParams(),
                        dataLoadParams = dataLoadParams,
                    ).collect { xapiDoc ->
                        val message = Message.obtain()
                        message.arg1 = incomingMessageId
                        message.what = IpcMessageBridgeWhatFlags.WHAT_FLOW_EMISSION
                        message.data = xapiDoc.toBundle(executor)
                        Log.d(
                            XapiIpcTags.LOGTAG,
                            "$logPrefix getAsFlow state emit ${xapiDoc.toPrettyString()}"
                        )
                        replyTo.send(message)
                    }
                }.also {
                    flowCollectors[incomingMessageId] = it
                }
            }

            XapiIpcMethodEnum.GET_MULTIDOC -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: getMultipleDocuments state")
                val requestHeaders = msg.data.getStringValues(XapiIpcKeys.KEY_HEADERS)?.let {
                    Headers.build { appendAll(it) }
                } ?: Headers.Empty
                val dataLoadParams = DataLoadParams(requestHeaders = requestHeaders)
                replyMessage.data = runBlocking {
                    docResource.getMultipleDocuments(
                        params = bundle.getMultiDocParams(),
                        dataLoadParams = dataLoadParams,
                    ).also {
                        Log.d(XapiIpcTags.LOGTAG, "$logPrefix getMultipleDocuments state: response ${it.toPrettyString()}")
                    }.toBundle(ListSerializer(String.serializer()), json)
                }

                msg.replyTo.send(replyMessage)
            }

            XapiIpcMethodEnum.POST -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: post state")
                val document = msg.data.toXapiDocument()
                runBlocking {
                    docResource.post(
                        params = bundle.getSingleDocParams(),
                        document = document,
                    )
                }
                replyMessage.data = Bundle().apply {
                    putInt(XapiIpcKeys.KEY_STATUS_CODE, 200)
                }

                msg.replyTo.send(replyMessage)
            }

            XapiIpcMethodEnum.PUT -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: put state")
                val document = msg.data.toXapiDocument()
                runBlocking {
                    docResource.put(
                        params = bundle.getSingleDocParams(),
                        document = document,
                    )
                }
                replyMessage.data = Bundle().apply {
                    putInt(XapiIpcKeys.KEY_STATUS_CODE, 200)
                }

                msg.replyTo.send(replyMessage)
            }

            XapiIpcMethodEnum.DELETE -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: delete state")
                runBlocking {
                    docResource.delete(
                        params = bundle.getSingleDocParams(),
                    )
                }
                replyMessage.data = Bundle().apply {
                    putInt(XapiIpcKeys.KEY_STATUS_CODE, 200)
                }

                msg.replyTo.send(replyMessage)
            }
        }
    }
}