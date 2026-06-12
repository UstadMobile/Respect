package world.respect.xapi.ipc.client

import android.os.Message
import world.respect.xapi.ipc.shared.messages.MessageReply

interface MessageRequestSender {

    /**
     * Send a Message as a request and receive a response
     */
    suspend fun sendRequest(message: Message): MessageReply


}