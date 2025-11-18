package world.respect.app.util

object DeepLinkConstants {
    const val SCHEME = "world.respect.app"
    const val HOST = "school-registered"
    const val PARAM_SCHOOL_URL = "schoolUrl"
    const val URI_PATTERN = "$SCHEME://$HOST?$PARAM_SCHOOL_URL={$PARAM_SCHOOL_URL}"
}