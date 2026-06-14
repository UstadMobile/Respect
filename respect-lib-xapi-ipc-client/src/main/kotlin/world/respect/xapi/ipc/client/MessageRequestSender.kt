package world.respect.xapi.ipc.client

import world.respect.xapi.ipc.shared.messages.MessageData

interface MessageRequestSender {

    /**
     * Send a Message as a request and receive a response
     */
    suspend fun sendRequest(messageData: MessageData): MessageData


}