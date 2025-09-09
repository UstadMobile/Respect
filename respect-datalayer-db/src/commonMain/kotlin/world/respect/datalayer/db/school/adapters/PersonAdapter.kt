package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.db.school.entities.PersonEntity
import world.respect.datalayer.db.school.entities.PersonEntityWithRoles
import world.respect.datalayer.db.school.entities.PersonRoleEntity
import world.respect.datalayer.school.model.Person
import world.respect.libxxhash.XXStringHasher
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class PersonEntities(
    val personEntity: PersonEntity,
    val personRoleEntities: List<PersonRoleEntity> = emptyList()
)

fun PersonEntityWithRoles.toPersonEntities() = PersonEntities(
    personEntity = person,
    personRoleEntities = roles
)

@OptIn(ExperimentalTime::class)
fun PersonEntities.toModel(): Person {
    return Person(
        guid = personEntity.pGuid,
        active = personEntity.pActive,
        lastModified = Instant.fromEpochMilliseconds(personEntity.pLastModified),
        stored = Instant.fromEpochMilliseconds(personEntity.pStored),
        username = personEntity.pUsername,
        givenName = personEntity.pGivenName,
        familyName = personEntity.pFamilyName,
        middleName = personEntity.pMiddleName,
        roles = emptyList(),
    )
}

@OptIn(ExperimentalTime::class)
fun Person.toEntities(
    xxStringHasher: XXStringHasher
): PersonEntities {
    return PersonEntities(
        personEntity = PersonEntity(
            pGuid = guid,
            pGuidHash = xxStringHasher.hash(guid),
            pActive = active,
            pLastModified = lastModified.toEpochMilliseconds(),
            pStored = stored.toEpochMilliseconds(),
            pUsername = username,
            pGivenName = givenName,
            pFamilyName = familyName,
            pMiddleName = middleName
        )
    )
}


