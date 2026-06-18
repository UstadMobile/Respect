package world.respect.datalayer.school.xapi

import kotlin.reflect.KClass

data class XapiLocalInvalidation(
    val entity: KClass<*>
)
