package world.respect.datalayer.respect.model

import kotlinx.serialization.Serializable

@Serializable
data class RespectReport(

    val reportId: String,

    val title: String,

    val reportOptions: String,

    val reportIsTemplate: Boolean = false,

    val active: Boolean = true,

)

