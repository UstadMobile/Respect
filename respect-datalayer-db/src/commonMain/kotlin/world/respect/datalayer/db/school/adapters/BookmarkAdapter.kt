package world.respect.datalayer.db.school.adapters

import androidx.room.Embedded
import androidx.room.Relation
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.db.shared.adapters.toModel
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.school.model.Bookmark
import world.respect.lib.opds.model.LangMap


/**
 * Publication + its relations
 */
data class OpdsPublicationEntities(

    @Embedded
    val publication: OpdsPublicationEntity,

    @Relation(
        parentColumn = "opeUid",
        entityColumn = "lmeTopParentUid1"
    )
    val langMaps: List<LangMapEntity> = emptyList(),

    @Relation(
        parentColumn = "opeUid",
        entityColumn = "rleOpdsParentUid"
    )
    val links: List<ReadiumLinkEntity> = emptyList()
)


/**
 * Bookmark + linked publication
 */
data class BookmarkEntities(

    @Embedded
    val bookmark: BookmarkEntity,

    @Relation(
        parentColumn = "bLearningUnitUrlHash",
        entityColumn = "opeUrlHash",
        entity = OpdsPublicationEntity::class
    )
    val publication: OpdsPublicationEntities? = null
)

fun BookmarkEntities.toModel(): Bookmark {

    fun List<LangMapEntity>.extract(propType: LangMapEntity.PropType): LangMap? {
        return this
            .filter { it.lmePropType == propType && it.lmeTopParentUid1 == 0L }
            .takeIf { it.isNotEmpty() }
            ?.toModel()
    }

    val title = publication?.langMaps
        ?.extract(LangMapEntity.PropType.OPDS_PUB_TITLE)

    return Bookmark(



        status = bookmark.bStatus,
        lastModified = bookmark.bLastModified,
        stored = bookmark.bStored,
        personUid = bookmark.bPersonUid,
        learningUnitManifestUrl = bookmark.bLearningUnitManifestUrl,
        title = title
    )
}

fun Bookmark.toEntities(
    uidNumberMapper: UidNumberMapper
): BookmarkEntities {

    val bookmarkEntity = BookmarkEntity(
        bStatus = status,
        bLastModified = lastModified,
        bStored = stored,
        bPersonUid = personUid,
        bPersonUidHash = uidNumberMapper(personUid),
        bLearningUnitManifestUrl = learningUnitManifestUrl,
        bLearningUnitUrlHash = uidNumberMapper(learningUnitManifestUrl)
    )

    return BookmarkEntities(
        bookmark = bookmarkEntity,
        publication = null
    )
}