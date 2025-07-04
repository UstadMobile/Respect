package world.respect.datasource.db.adapters

import world.respect.datasource.db.entities.LangMapEntity
import world.respect.datasource.db.entities.LangMapEntity.Companion.LANG_NONE
import world.respect.datasource.db.ext.langMapKey
import world.respect.datasource.opds.model.LangMap
import world.respect.datasource.opds.model.LangMapObjectValue
import world.respect.datasource.opds.model.LangMapStringValue

fun LangMap.toEntities(
    lmeTableId: Int,
    lmeEntityUid1: Long,
    lmeEntityUid2: Long = 0,
    lmePropId: Long = 0,
): List<LangMapEntity> {
    return when(this) {
        is LangMapStringValue -> {
            listOf(
                LangMapEntity(
                    lmeTableId = lmeTableId,
                    lmeEntityUid1 = lmeEntityUid1,
                    lmeEntityUid2 = lmeEntityUid2,
                    lmePropId = lmePropId,
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
                    lmeTableId = lmeTableId,
                    lmeEntityUid1 = lmeEntityUid1,
                    lmeEntityUid2 = lmeEntityUid2,
                    lmePropId = lmePropId,
                    lmeLang = langCode,
                    lmeRegion = region,
                    lmeValue = value,
                )
            }
        }
    }
}

fun List<LangMapEntity>.toLangMap(): LangMap {
    return if(this.size == 1 && this.first().lmeLang == LANG_NONE) {
        LangMapStringValue(this.first().lmeValue)
    }else {
        LangMapObjectValue(this.associate { it.langMapKey to it.lmeValue })
    }
}

