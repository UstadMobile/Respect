package world.respect.xapi.ipc.shared.messages

/**
 * When a request message is sent Message.arg2
 */
object XapiIpcResourceFlags {

    const val STATEMENTS_GET = 1

    const val STATEMENTS_GET_FLOW = 2

    const val STATEMENTS_POST = 3

    const val STATE_GET = 4

    const val STATE_GET_FLOW = 5

    const val STATE_GET_MULTIDOC = 6

    const val STATE_POST = 7

    const val STATE_PUT = 8

    const val STATE_DELETE = 9

    internal val FLAG_TO_ENUMS_MAP = mapOf(
        STATEMENTS_GET to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATEMENTS,
            XapiIpcMethodEnum.GET
        ),
        STATEMENTS_GET_FLOW to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATEMENTS,
            XapiIpcMethodEnum.GET_AS_FLOW
        ),
        STATEMENTS_POST to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATEMENTS,
            XapiIpcMethodEnum.POST
        ),
        STATE_GET to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATE,
            XapiIpcMethodEnum.GET
        ),
        STATE_GET_FLOW to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATE,
            XapiIpcMethodEnum.GET_AS_FLOW
        ),
        STATE_GET_MULTIDOC to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATE,
            XapiIpcMethodEnum.GET_MULTIDOC
        ),
        STATE_POST to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATE,
            XapiIpcMethodEnum.POST
        ),
        STATE_PUT to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATE,
            XapiIpcMethodEnum.PUT
        ),
        STATE_DELETE to XapiIpcResourceAndMethod(
            XapiIpcResourceEnum.STATE,
            XapiIpcMethodEnum.DELETE
        ),
    )

    internal val ENUMS_TO_FLAG_MAP = FLAG_TO_ENUMS_MAP.map { it.value to it.key }.toMap()

}