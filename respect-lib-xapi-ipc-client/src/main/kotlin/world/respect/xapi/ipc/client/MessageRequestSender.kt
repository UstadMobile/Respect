package world.respect.xapi.ipc.client

import android.os.Message

interface MessageRequestSender {

    /**
     * Send a Message as a request and receive a response
     */
    suspend fun sendRequest(message: Message): Message


}