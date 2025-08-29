package world.respect.datalayer.db.oneroaster.adapter

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import world.respect.datalayer.db.oneroster.entities.OneRosterClassEntity
import world.respect.datalayer.oneroster.model.OneRosterBaseStatusEnum
import world.respect.datalayer.oneroster.model.OneRosterClass
import world.respect.libxxhash.XXStringHasher
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class OneRoasterClassEntities(
    val oneRoasterClassEntity: OneRosterClassEntity,
)

@OptIn(ExperimentalTime::class)
fun OneRoasterClassEntities.toModel(): OneRosterClass {
    return OneRosterClass(
        sourcedId = oneRoasterClassEntity.classSourcedId,
        status = OneRosterBaseStatusEnum.valueOf(oneRoasterClassEntity.classStatus),
        dateLastModified = Instant.fromEpochMilliseconds(oneRoasterClassEntity.classDateLastModified),
        title=oneRoasterClassEntity.classTitle,
        location = oneRoasterClassEntity.classLocation,
        metadata = oneRoasterClassEntity.classMetadata?.let {
            Json.parseToJsonElement(it).jsonObject
        }
    )
}

@OptIn(ExperimentalTime::class)
fun OneRosterClass.toEntities(
    xxStringHasher: XXStringHasher
): OneRoasterClassEntities {
    return OneRoasterClassEntities(
        oneRoasterClassEntity = OneRosterClassEntity(
            classSourcedId = sourcedId,
            classStatus = status.name,
            classDateLastModified = dateLastModified.toEpochMilliseconds(),
            classTitle = title,
            classLocation = location,
            classMetadata = metadata?.toString()
        )
    )
}


