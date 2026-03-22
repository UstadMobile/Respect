package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.VerbEntity
import world.respect.datalayer.db.school.xapi.entities.VerbLangMapEntry
import world.respect.datalayer.school.xapi.model.XapiVerb

data class VerbEntities(
    val verbEntity: VerbEntity,
    val verbLangMapEntries: List<VerbLangMapEntry>,
)

fun XapiVerb.toVerbEntities(
    uidNumberMapper: UidNumberMapper,
): VerbEntities {
    val verbIri = id
    val verbUid = uidNumberMapper(verbIri)

    return VerbEntities(
        verbEntity = VerbEntity(
            verbUid = verbUid,
            verbUrlId = id,
        ),
        verbLangMapEntries = display?.entries?.map {
            VerbLangMapEntry(
                vlmeVerbUid = verbUid,
                vlmeEntryString = it.value,
                vlmeLangCode = it.key,
            )
        } ?: emptyList(),
    )
}

fun VerbEntities.toModel(): XapiVerb {
    return XapiVerb(
        id = verbEntity.verbUrlId,
        display = verbLangMapEntries.associate { it.vlmeLangCode to it.vlmeEntryString },
    )
}
