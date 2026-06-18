package world.respect.xapi.ipc.client

import world.respect.xapi.ipc.shared.messages.MessageData

interface XapiMessageBridge {

    /**
     * Send a Message as a request and receive a response
     */
    suspend fun executeRequest(messageData: MessageData): MessageData


}