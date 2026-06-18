package world.respect.shared.ext

import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiObjectType


/**
 * Creates a students XapiGroup from this XapiActivity representing a class.
 * The group name is derived from the activity definition name (first available value).
 */
fun XapiActivity.studentsXapiGroup(): XapiGroup {
    val className = definition?.name?.values?.firstOrNull()
    return XapiGroup(
        name = "${className ?: ""} students",
        account = XapiAccount(
            homePage = id,
            name = "students"
        ),
        objectType = XapiObjectType.Group,
    )
}

