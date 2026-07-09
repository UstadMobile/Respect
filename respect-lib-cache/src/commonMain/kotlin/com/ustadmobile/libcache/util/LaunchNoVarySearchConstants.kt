package com.ustadmobile.libcache.util

/**
 * The href to launch a lesson needs to have NoVarySearch params applied as documented
 */
object LaunchNoVarySearchConstants {

    val LAUNCH_NO_VARY_PARAM_NAMES = listOf(
        "endpoint",
        "actor",
        "auth",
        "activity_id",
        "registration",
        "respectLaunchVersion",
        "xapiIpcPackage",
    )

    val LAUNCH_LINK_NO_VARY_HEADER = buildString {
        append("params=(")
        append(LAUNCH_NO_VARY_PARAM_NAMES.joinToString(separator = " ") { "\"$it\"" })
        append(")")
    }

}