package org.openeel.lib.ipc.messagebridge

object IpcMessageBridgeWhatFlags {

    const val WHAT_REQUEST = 1

    const val WHAT_RESPONSE = 2

    const val WHAT_FLOW_EMISSION = 3

    /**
     * Sent by the client when a flow is completed so that the service can stop collecting the flow
     */
    const val WHAT_FLOW_COMPLETION = 4



}