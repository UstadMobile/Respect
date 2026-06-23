package world.respect.xapi.ipc.shared.messages

object XapiIpcIntent {

    /**
     * Used by other apps to connect to Xapi IPC service
     */
    @Suppress("unused")
    const val ACTION_XAPI_OVER_IPC = "org.openeel.action.xapioveripc"

    /**
     * A parameter that is added when launching a unit.
     */
    const val PARAM_NAME_IPC_PACKAGE = "xapiIpcPackage"

}