package world.respect.shared.domain.account

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Person

@Serializable
data class RespectSessionAndPerson(
    val session: RespectSession,
    val person: Person,
)
