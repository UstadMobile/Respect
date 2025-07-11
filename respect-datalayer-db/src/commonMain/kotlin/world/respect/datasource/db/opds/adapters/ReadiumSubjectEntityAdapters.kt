package world.respect.datasource.db.opds.adapters

import kotlinx.serialization.json.Json
import world.respect.datasource.db.opds.OpdsParentType
import world.respect.datasource.db.opds.entities.ReadiumLinkEntity
import world.respect.datasource.db.opds.entities.ReadiumSubjectEntity
import world.respect.datasource.db.shared.adapters.asEntities
import world.respect.datasource.db.shared.adapters.toModel
import world.respect.datasource.db.shared.entities.LangMapEntity
import world.respect.datasource.opds.model.ReadiumSubject
import world.respect.datasource.opds.model.ReadiumSubjectObject
import world.respect.datasource.opds.model.ReadiumSubjectStringValue
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

class ReadiumSubjectEntities(
    val readiumSubject: ReadiumSubjectEntity,
    val langMapEntities: List<LangMapEntity>,
    val readiumLinkEntities: List<ReadiumLinkEntity>,
)

fun ReadiumSubject.asEntities(
    primaryKeyGenerator: PrimaryKeyGenerator,
    json: Json,
    topParentType: OpdsParentType,
    topParentUid: Long,
    index: Int,
): ReadiumSubjectEntities {
    val rseUid = primaryKeyGenerator.nextId(ReadiumSubjectEntity.TABLE_ID)
    val subjectObject = this as? ReadiumSubjectObject

    return ReadiumSubjectEntities(
        readiumSubject = ReadiumSubjectEntity(
            rseUid = rseUid,
            rseStringValue = (this as? ReadiumSubjectStringValue)?.value,
            rseTopParentType = topParentType,
            rseTopParentUid = topParentUid,
            rseSubjectSortAs = subjectObject?.sortAs,
            rseSubjectCode = subjectObject?.code,
            rseSubjectScheme = subjectObject?.scheme,
            rseIndex = index,
        ),
        langMapEntities = subjectObject?.name?.asEntities(
            lmeTopParentType = topParentType.langMapTopParentType,
            lmeTopParentUid1 = topParentUid,
            lmePropType = LangMapEntity.PropType.READIUM_SUBJECT_NAME,
            lmePropFk = rseUid,
        ) ?: emptyList(),
        readiumLinkEntities = subjectObject?.links?.mapIndexed { linkIndex, link ->
            link.asEntities(
                pkGenerator = primaryKeyGenerator,
                json = json,
                opdsParentType = topParentType,
                opdsParentUid = topParentUid,
                rlePropType = ReadiumLinkEntity.PropertyType.READIUM_SUBJECT_LINKS,
                rlePropFk = rseUid,
                rleIndex = linkIndex,
            )
        }?.flatten() ?: emptyList(),
    )
}

fun ReadiumSubjectEntities.asModel(json: Json): ReadiumSubject {
    return if(this.readiumSubject.rseStringValue != null) {
        ReadiumSubjectStringValue(this.readiumSubject.rseStringValue)
    }else {
        ReadiumSubjectObject(
            name = langMapEntities.toModel(),
            sortAs = readiumSubject.rseSubjectSortAs,
            code = readiumSubject.rseSubjectCode,
            scheme = readiumSubject.rseSubjectScheme,
            links = readiumLinkEntities.asModels(
                json = json,
                propType = ReadiumLinkEntity.PropertyType.READIUM_SUBJECT_LINKS,
                propFk = readiumSubject.rseUid,
            ),
        )
    }
}

