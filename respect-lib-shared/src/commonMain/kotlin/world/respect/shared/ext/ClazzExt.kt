package world.respect.shared.ext

import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.shared.domain.enrollments.UpdateClazzStudentXapiGroupUseCase.Companion.STUDENTS


/**
 * Creates a students XapiGroup from this XapiActivity representing a class.
 * The group name is derived from the activity definition name (first available value).
 */
fun XapiActivity.studentsXapiGroup(): XapiGroup {
    val className = definition?.name?.values?.firstOrNull()
    return XapiGroup(
        name = "$className $STUDENTS",
        account = XapiAccount(
            homePage = id,
            name = STUDENTS
        ),
        objectType = XapiObjectType.Group,
    )
}


