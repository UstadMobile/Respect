package world.respect.datalayer.db.school.adapters

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.opds.adapters.asModels
import world.respect.datalayer.db.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.db.shared.adapters.toModel
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.school.model.Bookmark
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.ReadiumLink

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
        parentColumn = "bUrl",
        entityColumn = "opeUrlHash",
        entity = OpdsPublicationEntity::class

    )
    val publication: OpdsPublicationEntities? = null
)

fun BookmarkEntities.toModel(
    json: Json
): Bookmark {

    fun List<LangMapEntity>.extract(
        propType: LangMapEntity.PropType,
        publicationUid: Long
    ): LangMap? {
        return this
            .filter {
                it.lmePropType == propType &&
                        it.lmeTopParentUid1 == publicationUid
            }
            .takeIf { it.isNotEmpty() }
            ?.toModel()
    }

    fun List<ReadiumLinkEntity>.asModelsSub(
        propType: ReadiumLinkEntity.PropertyType
    ): List<ReadiumLink> {
        val pubUid = publication?.publication?.opeUid ?: return emptyList()
        return asModels(
            json = json,
            propType = propType,
            propFk = pubUid
        )
    }

    val title = publication?.langMaps?.extract(
        LangMapEntity.PropType.OPDS_PUB_TITLE,
        publication.publication.opeUid
    )

    val subTitle = publication?.langMaps?.extract(
        LangMapEntity.PropType.OPDS_PUB_SUBTITLE,
        publication.publication.opeUid
    )

    val images = publication?.links?.asModelsSub(
        ReadiumLinkEntity.PropertyType.OPDS_PUB_IMAGES
    )

    val imageUrl = images?.firstOrNull()?.href

    return Bookmark(
        status = bookmark.bStatus,
        lastModified = bookmark.bLastModified,
        stored = bookmark.bStored,
        personUid = bookmark.bPersonUid,
        learningUnitManifestUrl = bookmark.bUrl,
        title = title,
        subTitle = subTitle,
        imageUrl = imageUrl
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
        bUrl = learningUnitManifestUrl,
        bUrlHash = uidNumberMapper(learningUnitManifestUrl.toString())
    )

    return BookmarkEntities(
        bookmark = bookmarkEntity,
        publication = null
    )
}