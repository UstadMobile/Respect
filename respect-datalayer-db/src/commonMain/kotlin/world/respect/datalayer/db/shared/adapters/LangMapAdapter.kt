package world.respect.datalayer.db.shared.adapters

import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.db.shared.entities.LangMapEntity.Companion.LANG_NONE
import world.respect.datalayer.db.shared.ext.langMapKey
import world.respect.datalayer.opds.model.LangMap
import world.respect.datalayer.opds.model.LangMapObjectValue
import world.respect.datalayer.opds.model.LangMapStringValue

fun LangMap.asEntities(
    lmeTopParentType: LangMapEntity.TopParentType,
    lmeTopParentUid1: Long,
    lmeTopParentUid2: Long = 0,
    lmePropFk: Long,
    lmePropType: LangMapEntity.PropType,
): List<LangMapEntity> {
    return when(this) {
        is LangMapStringValue -> {
            listOf(
                LangMapEntity(
                    lmeTopParentType = lmeTopParentType,
                    lmeTopParentUid1 = lmeTopParentUid1,
                    lmeTopParentUid2 = lmeTopParentUid2,
                    lmePropType = lmePropType,
                    lmePropFk = lmePropFk,
                    lmeLang = "",
                    lmeRegion = null,
                    lmeValue = value,
                )
            )
        }
        is LangMapObjectValue -> {
            map.map { (key, value) ->
                val (langCode, region) = if(key.contains('-')) {
                    key.split('-', limit = 2).let {
                        it.first() to it.getOrNull(1)
                    }
                }else {
                    key to null
                }

                LangMapEntity(
                    lmeTopParentType = lmeTopParentType,
                    lmeTopParentUid1 = lmeTopParentUid1,
                    lmeTopParentUid2 = lmeTopParentUid2,
                    lmePropType = lmePropType,
                    lmePropFk = lmePropFk,
                    lmeLang = langCode,
                    lmeRegion = region,
                    lmeValue = value,
                )
            }
        }
    }
}

fun List<LangMapEntity>.toModel(): LangMap {
    return if(this.size == 1 && this.first().lmeLang == LANG_NONE) {
        LangMapStringValue(this.first().lmeValue)
    }else {
        LangMapObjectValue(this.associate { it.langMapKey to it.lmeValue })
    }
}

