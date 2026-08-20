package org.openeel.lib.ipc.messagebridge.ext

import android.os.Message
import org.openeel.lib.ipc.messagebridge.MessageData

fun Message.setFromMessageData(
    messageData: MessageData
) {
    data = messageData.data
    what = messageData.what
    arg1 = messageData.arg1
    arg2 = messageData.arg2
}
