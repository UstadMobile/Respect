package world.respect.xapi.ipc.client

import kotlinx.coroutines.flow.Flow
import world.respect.xapi.ipc.shared.messages.MessageData

interface XapiMessageBridge {

    /**
     * Send MessageData over a Messenger and then wait for a single response: used to run a single
     * request-reply interaction (e.g. get statements, post statements, etc).
     *
     * The bridge will set a message ID and then wait for a reply with the same ID.
     *
     * @param messageData the message to send
     * @return MessageData response for the given message
     */
    suspend fun executeForResponse(messageData: MessageData): MessageData

    /**
     * Send MessageData over a Messenger and then collect a flow of responses: used to run flow
     * response types (e.g. offline first flows)
     *
     * @param messageData the message to send
     * @return a flow of MessageData responses to the message sent
     */
    fun executeForFlow(messageData: MessageData): Flow<MessageData>

    fun close()


}