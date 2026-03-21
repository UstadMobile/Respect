package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class XapiAccount(
    val homePage: String,
    val name: String,
)
