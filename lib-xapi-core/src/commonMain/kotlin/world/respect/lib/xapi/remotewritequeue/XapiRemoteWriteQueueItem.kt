package world.respect.lib.xapi.remotewritequeue

data class XapiRemoteWriteQueueItem(
    val xrqItemId: Int = 0,
    val method: Method,
    val resource: Resource,
    val itemId: String,
) {

    enum class Method(
        val flag : Int
    ) {

        POST(1),

        PUT(2),

        DELETE(3),

        ;

        companion object {

            fun fromFlag(flag: Int) = entries.first { it.flag == flag }

        }

    }

    enum class Resource(val flag: Int) {

        STATEMENTS(1),

        ACTIVITY_PROFILE(2),
    }


}