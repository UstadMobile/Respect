package world.respect.datasource.db.opds.adapters

import kotlinx.serialization.json.Json
import world.respect.datasource.DataLoadResult
import world.respect.datasource.db.opds.OpdsParentType
import world.respect.datasource.db.opds.entities.OpdsFeedEntity
import world.respect.datasource.db.opds.entities.OpdsFeedMetadataEntity
import world.respect.datasource.db.opds.entities.OpdsGroupEntity
import world.respect.datasource.db.opds.entities.OpdsPublicationEntity
import world.respect.datasource.db.opds.entities.ReadiumLinkEntity
import world.respect.datasource.db.opds.entities.ReadiumLinkEntity.PropertyType.OPDS_FEED_LINKS
import world.respect.datasource.db.opds.entities.ReadiumLinkEntity.PropertyType.OPDS_FEED_NAVIGATION
import world.respect.datasource.db.shared.entities.LangMapEntity
import world.respect.datasource.opds.model.OpdsFeed
import world.respect.datasource.opds.model.ReadiumLink
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libxxhash.XXStringHasher

class OpdsFeedEntities(
    val opdsFeed: OpdsFeedEntity,
    val feedMetaData: List<OpdsFeedMetadataEntity>,
    val langMapEntities: List<LangMapEntity>,
    val linkEntities: List<ReadiumLinkEntity>,
    val publications: List<OpdsPublicationEntity>,
    val groups: List<OpdsGroupEntity>
)

fun DataLoadResult<OpdsFeed>.asEntities(
    json: Json,
    primaryKeyGenerator: PrimaryKeyGenerator,
    xxStringHasher: XXStringHasher,
) : OpdsFeedEntities? {
    val url = metaInfo.requireUrl()
    val opdsFeed = this.data ?: return null

    val ofeUid = xxStringHasher.hash(metaInfo.requireUrl().toString())

    fun List<ReadiumLink>?.asEntitiesSub(
        propType: ReadiumLinkEntity.PropertyType,
    ) : List<ReadiumLinkEntity> {
        return this?.mapIndexed { index, link ->
            link.asEntities(
                pkGenerator = primaryKeyGenerator,
                json = json,
                opdsParentType = OpdsParentType.OPDS_FEED,
                opdsParentUid = ofeUid,
                rlePropType = propType,
                rlePropFk = ofeUid,
                rleIndex = index,
            )
        }?.flatten() ?: emptyList()
    }

    val feedMetadata = opdsFeed.metadata.asEntity(
        ofmeOfeUid = ofeUid,
        ofmePropType = OpdsFeedMetadataEntity.PropType.FEED_METADATA,
        ofmeRelUid = ofeUid,
    )

    val groupEntities = opdsFeed.groups?.mapIndexed { index, group ->
        group.asEntities(
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            xxStringHasher = xxStringHasher,
            ofeUid = ofeUid,
            index = index,
        )
    } ?: emptyList()

    val publicationEntities = opdsFeed.publications?.mapIndexed { index, publication ->
        publication.asEntities(
            dataLoadResult = null,
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            xxStringHasher = xxStringHasher,
            feedUid = ofeUid,
            groupUid = 0,
            feedIndex = index,
        )
    } ?: emptyList()

    return OpdsFeedEntities(
        opdsFeed = OpdsFeedEntity(
            ofeUid = ofeUid,
            ofeUrl = url,
            ofeUrlHash = ofeUid,
            ofeLastModifiedHeader = metaInfo.lastModified
        ),
        feedMetaData = buildList {
            add(feedMetadata)
            addAll(groupEntities.map { it.metadata })
        },
        langMapEntities = buildList {
            addAll(publicationEntities.flatMap { it.langMapEntities })
            addAll(groupEntities.flatMap { it.langMapEntities })
        },
        linkEntities = buildList {
            addAll(
                opdsFeed.links.asEntitiesSub(OPDS_FEED_LINKS)
            )
            addAll(
                opdsFeed.navigation.asEntitiesSub(OPDS_FEED_NAVIGATION)
            )

            addAll(publicationEntities.flatMap { it.linkEntities })
            addAll(groupEntities.flatMap { it.links })
        },
        publications = buildList {
            addAll(publicationEntities.map { it.opdsPublicationEntity } )
            addAll(groupEntities.flatMap { it.publications })
        },
        groups = groupEntities.map { it.group }
    )
}

fun OpdsFeedEntities.asModel(
    json: Json
): DataLoadResult<OpdsFeed> {
    val feedUid = opdsFeed.ofeUid
    return DataLoadResult(
        data = OpdsFeed(
            metadata = feedMetaData.first { it.ofmeOfeUid == feedUid }.asModel(),
            links = linkEntities.asModels(json, OPDS_FEED_LINKS, feedUid),
            publications = publications.filter {
                it.opeOfeUid == feedUid && it.opeOgeUid == 0L
            }.mapNotNull { publication ->
                OpdsPublicationEntities(
                    opdsPublicationEntity = publication,
                    langMapEntities = langMapEntities.filter {
                        it.lmeTopParentUid1 == publication.opeUid
                    },
                    linkEntities = linkEntities.filter { link ->
                        link.rleOpdsParentUid == publication.opeUid
                    }
                ).asModel(json).data
            },
            navigation = linkEntities.asModels(json, OPDS_FEED_NAVIGATION, feedUid),
            facets = emptyList(), //TODO
            groups = groups.map { groupEntity ->
                OpdsGroupEntities(
                    group = groupEntity,
                    metadata = feedMetaData.first { it.ofmePropFk == groupEntity.ogeUid },
                    publications = publications.filter { it.opeOgeUid == groupEntity.ogeUid },
                    links = linkEntities,
                    langMapEntities = langMapEntities,
                ).asModel(json)
            }
        )
    )
}

