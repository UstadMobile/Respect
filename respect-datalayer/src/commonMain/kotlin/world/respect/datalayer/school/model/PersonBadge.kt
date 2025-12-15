package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601

@Serializable
data class PersonBadge(
    val personGuid: String,
    val qrCodeUrl: String,
    override val lastModified: InstantAsISO8601,
    override val stored: InstantAsISO8601,
):  ModelWithTimes
