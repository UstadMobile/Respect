package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.IndicatorEntity
import world.respect.datalayer.school.model.Indicator
import kotlin.time.Instant

data class IndicatorEntities(
    val indicator: IndicatorEntity
)
fun IndicatorEntities.toModel(): Indicator {
    return Indicator(
        indicatorId = indicator.iGuid,
        name = indicator.iName,
        description = indicator.iDescription,
        type = indicator.iType,
        sql = indicator.iSql,
        stored = Instant.fromEpochMilliseconds(indicator.iStored),
        lastModified = Instant.fromEpochMilliseconds(indicator.iLastModified),

        )
}

fun Indicator.toEntities(
    uidNumberMapper: UidNumberMapper
): IndicatorEntities {
    val rGuidHash = uidNumberMapper(indicatorId)
    return IndicatorEntities(indicator = IndicatorEntity(
        iGuid = indicatorId,
        iGuidHash = rGuidHash,
        iName = name,
        iDescription = description,
        iType = type,
        iSql = sql,
        iLastModified = lastModified.toEpochMilliseconds(),
        iStored = stored.toEpochMilliseconds(),
    ))
}