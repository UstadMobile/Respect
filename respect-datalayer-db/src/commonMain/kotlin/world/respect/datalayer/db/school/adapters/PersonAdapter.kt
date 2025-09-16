package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.PersonEntity
import world.respect.datalayer.db.school.entities.PersonEntityWithRoles
import world.respect.datalayer.db.school.entities.PersonRoleEntity
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRole
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
        status = personEntity.pStatus,
        userActive = personEntity.pActive,
        lastModified = Instant.fromEpochMilliseconds(personEntity.pLastModified),
        stored = Instant.fromEpochMilliseconds(personEntity.pStored),
        metadata = personEntity.pMetadata,
        username = personEntity.pUsername,
        givenName = personEntity.pGivenName,
        familyName = personEntity.pFamilyName,
        middleName = personEntity.pMiddleName,
        roles = personRoleEntities.map {
            PersonRole(
                isPrimaryRole = it.prIsPrimaryRole,
                roleEnum = it.prRoleEnum,
                beginDate = it.prBeginDate,
                endDate = it.prEndDate,
            )
        },
    )
}

fun Person.toEntities(
    uidNumberMapper: UidNumberMapper
): PersonEntities {
    val pGuidHash = uidNumberMapper(guid)
    return PersonEntities(
        personEntity = PersonEntity(
            pGuid = guid,
            pGuidHash = pGuidHash,
            pActive = userActive,
            pStatus = status,
            pLastModified = lastModified.toEpochMilliseconds(),
            pStored = stored.toEpochMilliseconds(),
            pMetadata = metadata,
            pUsername = username,
            pGivenName = givenName,
            pFamilyName = familyName,
            pMiddleName = middleName,
        ),
        personRoleEntities = roles.map {
            PersonRoleEntity(
                prPersonGuidHash = pGuidHash,
                prIsPrimaryRole = it.isPrimaryRole,
                prRoleEnum = it.roleEnum,
                prEndDate = it.endDate,
                prBeginDate = it.beginDate,
            )
        }
    )
}


