package world.respect.xapi.ipc.shared.messages.ext

import android.os.Message
import world.respect.xapi.ipc.shared.messages.MessageData

fun Message.setFromMessageData(
    messageData: MessageData
) {
    data = messageData.data
    what = messageData.what
    arg1 = messageData.arg1
    arg2 = messageData.arg2
}
