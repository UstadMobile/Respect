package world.respect.datalayer.db.school.xapi.ext

import world.respect.datalayer.db.school.xapi.entities.ActivityLangMapEntry

fun List<ActivityLangMapEntry>.toLangMap(
    predicate: (ActivityLangMapEntry) -> Boolean
): Map<String, String> {
    return this.filter(predicate).associate {
        it.almeLangCode to it.almeValue
    }
}