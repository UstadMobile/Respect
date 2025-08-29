package world.respect.datalayer.db.oneroaster.adapter

import kotlinx.serialization.json.jsonObject
import world.respect.datalayer.db.oneroaster.entities.OneRosterUserEntity
import world.respect.datalayer.oneroster.model.OneRosterBaseStatusEnum
import world.respect.datalayer.oneroster.model.OneRosterUser
import world.respect.libxxhash.XXStringHasher
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import world.respect.datalayer.oneroster.model.OneRosterResourceGUIDRef
import world.respect.datalayer.oneroster.model.OneRosterRole
import world.respect.datalayer.oneroster.model.OneRosterUserId
import world.respect.datalayer.oneroster.model.OneRosterUserProfile


data class OneRoasterUserEntities(
    val oneRosterUserEntity: OneRosterUserEntity,
)

fun OneRoasterUserEntities.toModel(): OneRosterUser {
    return OneRosterUser(
        sourcedId = oneRosterUserEntity.userSourcedId,
        status = OneRosterBaseStatusEnum.valueOf(oneRosterUserEntity.userStatus),
        dateLastModified = Instant.fromEpochMilliseconds(oneRosterUserEntity.userDateLastModified),
        metadata = oneRosterUserEntity.userMetadata?.let {
            Json.parseToJsonElement(it).jsonObject
        },
        identifier = oneRosterUserEntity.identifier,
        username = oneRosterUserEntity.username,
        userIds = oneRosterUserEntity.userIds
            ?.let { Json.decodeFromString<List<OneRosterUserId>>(it) } ?: emptyList(),
        enabledUser = oneRosterUserEntity.enabledUser,
        givenName = oneRosterUserEntity.givenName,
        familyName = oneRosterUserEntity.familyName,
        middleName = oneRosterUserEntity.middleName,
        preferredFirstName = oneRosterUserEntity.preferredFirstName,
        preferredMiddleName = oneRosterUserEntity.preferredMiddleName,
        userMasterIdentifier = oneRosterUserEntity.userMasterIdentifier,
        preferredLastName = oneRosterUserEntity.preferredLastName,
        pronouns = oneRosterUserEntity.pronouns,
        roles = oneRosterUserEntity.roles
            ?.let { Json.decodeFromString<List<OneRosterRole>>(it) } ?: emptyList(),
        userProfiles = oneRosterUserEntity.userProfiles
            ?.let { Json.decodeFromString<List<OneRosterUserProfile>>(it) } ?: emptyList(),
        email = oneRosterUserEntity.email,
        sms = oneRosterUserEntity.sms,
        phone = oneRosterUserEntity.phone,
        grades = oneRosterUserEntity.grades?.let { Json.decodeFromString<List<String>>(it) }
            ?: emptyList(),
        password = oneRosterUserEntity.password,
        resources = oneRosterUserEntity.resources?.let { Json.decodeFromString<List<OneRosterResourceGUIDRef>>(it) }
            ?:emptyList(),

        )
}

fun OneRosterUser.toEntities(
    xxStringHasher: XXStringHasher
): OneRoasterUserEntities {
    return OneRoasterUserEntities(
        oneRosterUserEntity = OneRosterUserEntity(
            userSourcedId = sourcedId,
            userStatus = status.name,
            userDateLastModified = dateLastModified.toEpochMilliseconds(),
            userMetadata = metadata?.toString(),
            userMasterIdentifier = userMasterIdentifier,
            username = username,
            userIds = userIds?.let { Json.encodeToString(it) },
            enabledUser = enabledUser,
            givenName = givenName,
            familyName = familyName,
            middleName = middleName,
            preferredFirstName = preferredFirstName,
            preferredMiddleName = preferredMiddleName,
            preferredLastName = preferredLastName,
            pronouns = pronouns,
            roles = roles.let{Json.encodeToString(it)},
            userProfiles = userProfiles.let { Json.encodeToString(it) },

            identifier = identifier,
            email = email,
            sms = sms,
            phone = phone,
            grades = grades.let { Json.encodeToString(it) },
            password = password,
            resources = resources.let { Json.encodeToString(it) },
        )
    )

}
