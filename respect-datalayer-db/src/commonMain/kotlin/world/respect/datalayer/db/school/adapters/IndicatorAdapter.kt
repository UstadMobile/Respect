package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.db.school.entities.IndicatorEntity
import world.respect.datalayer.school.model.Indicator
import world.respect.libxxhash.XXStringHasher

fun IndicatorEntity.toIndicator(): Indicator {
    return Indicator(
        indicatorId = iGuid,
        name = iName,
        description = iDescription,
        type = iType,
        sql = iSql
    )
}

fun Indicator.toIndicatorEntity(
    hasher: XXStringHasher,
): IndicatorEntity {
    return IndicatorEntity(
        iGuid = indicatorId,
        iGuidHash = hasher.hash(indicatorId),
        iName = name,
        iDescription = description,
        iType = type,
        iSql = sql
    )
}