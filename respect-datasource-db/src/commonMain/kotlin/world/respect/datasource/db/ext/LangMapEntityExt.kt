package world.respect.datasource.db.ext

import world.respect.datasource.db.entities.LangMapEntity

val LangMapEntity.langMapKey: String
    get() = if(lmeRegion != null) {
        "$lmeLang-$lmeRegion"
    }else {
        lmeLang
    }