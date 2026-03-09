package world.respect.datalayer.db.school.adapters

import androidx.room.Embedded
import androidx.room.Relation
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.school.entities.AssignmentEntity
import world.respect.datalayer.db.school.entities.AssignmentLearningResourceRefEntity
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.school.model.Bookmark

data class PublicationWithDetails(

    @Embedded
    val publication: OpdsPublicationEntity,

    @Relation(
        parentColumn = "opeUid",
        entityColumn = "lmeTopParentUid1"
    )
    val langMapEntities: List<LangMapEntity> = emptyList(),

    @Relation(
        parentColumn = "opeUid",
        entityColumn = "rleOpdsParentUid"
    )
    val linkEntities: List<ReadiumLinkEntity> = emptyList()
)
data class BookmarkEntities(

    @Embedded
    val bookmark: BookmarkEntity,

    @Relation(
        parentColumn = "bLearningUnitUrlHash",
        entityColumn = "opeUrlHash",
        entity = OpdsPublicationEntity::class
    )
    val publication: PublicationWithDetails?
)
fun BookmarkEntity.toModel(): Bookmark {
    return Bookmark(
        status = bStatus,
        lastModified = bLastModified,
        stored = bStored,
        personUid = bPersonUid,
        learningUnitManifestUrl = bLearningUnitManifestUrl,
        title = "",
        subtitle = "",
        imageUrl = ""
    )
}

fun Bookmark.toEntities(uidNumberMapper: UidNumberMapper,): BookmarkEntity {
    return BookmarkEntity(
        bStatus = status,
        bLastModified = lastModified,
        bStored = stored,
        bPersonUid = personUid,
        bPersonUidHash = uidNumberMapper(personUid),
        bLearningUnitManifestUrl = learningUnitManifestUrl,
        bLearningUnitUrlHash = uidNumberMapper(learningUnitManifestUrl)
    )
}

fun BookmarkEntities.toModel(): Bookmark {

    val title = publication?.langMapEntities
        ?.firstOrNull { it.lmePropType == LangMapEntity.PropType.OPDS_PUB_TITLE }
        ?.lmeValue

    val subtitle = publication?.langMapEntities
        ?.firstOrNull { it.lmePropType == LangMapEntity.PropType.OPDS_PUB_SUBTITLE }
        ?.lmeValue

    val imageHref = publication?.linkEntities
        ?.firstOrNull { it.rlePropType == ReadiumLinkEntity.PropertyType.OPDS_PUB_IMAGES }
        ?.rleHref

    return Bookmark(
        personUid = bookmark.bPersonUid,
        learningUnitManifestUrl = bookmark.bLearningUnitManifestUrl,
        status = bookmark.bStatus,
        lastModified = bookmark.bLastModified,
        stored = bookmark.bStored,
        title = title,
        subtitle = subtitle,
        imageUrl = imageHref
    )
}