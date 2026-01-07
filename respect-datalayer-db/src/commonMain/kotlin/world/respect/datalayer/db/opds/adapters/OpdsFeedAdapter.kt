package world.respect.datalayer.db.opds.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.opds.OpdsParentType
import world.respect.datalayer.db.opds.entities.OpdsFeedEntity
import world.respect.datalayer.db.opds.entities.OpdsFeedMetadataEntity
import world.respect.datalayer.db.opds.entities.OpdsGroupEntity
import world.respect.datalayer.db.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity.PropertyType.OPDS_FEED_LINKS
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity.PropertyType.OPDS_FEED_NAVIGATION
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

class OpdsFeedEntities(
    val opdsFeed: OpdsFeedEntity,
    val feedMetaData: List<OpdsFeedMetadataEntity>,
    val langMapEntities: List<LangMapEntity>,
    val linkEntities: List<ReadiumLinkEntity>,
    val publications: List<OpdsPublicationEntity>,
    val groups: List<OpdsGroupEntity>
)

fun DataReadyState<OpdsFeed>.asEntities(
    json: Json,
    primaryKeyGenerator: PrimaryKeyGenerator,
    uidNumberMapper: UidNumberMapper,
) : OpdsFeedEntities? {
    val url = metaInfo.requireUrl()

    val ofeUid = uidNumberMapper(metaInfo.requireUrl().toString())

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

    val feedMetadata = data.metadata.asEntity(
        ofmeOfeUid = ofeUid,
        ofmePropType = OpdsFeedMetadataEntity.PropType.FEED_METADATA,
        ofmeRelUid = ofeUid,
    )

    val groupEntities = data.groups?.mapIndexed { index, group ->
        group.asEntities(
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            uidNumberMapper = uidNumberMapper,
            ofeUid = ofeUid,
            index = index,
        )
    } ?: emptyList()

    val publicationEntities = data.publications?.mapIndexed { index, publication ->
        publication.asEntities(
            dataLoadResult = null,
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            uidNumberMapper = uidNumberMapper,
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
            ofeLastModifiedHeader = metaInfo.lastModified,
            ofeEtag = metaInfo.etag,
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
                data.links.asEntitiesSub(OPDS_FEED_LINKS)
            )
            addAll(
                data.navigation.asEntitiesSub(OPDS_FEED_NAVIGATION)
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
): DataReadyState<OpdsFeed> {
    val feedUid = opdsFeed.ofeUid
    return DataReadyState(
        data = OpdsFeed(
            metadata = feedMetaData.first { it.ofmeOfeUid == feedUid }.asModel(),
            links = linkEntities.asModels(json, OPDS_FEED_LINKS, feedUid),
            publications = publications.filter {
                it.opeOfeUid == feedUid && it.opeOgeUid == 0L
            }.map { publication ->
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
        ),
        metaInfo = DataLoadMetaInfo(
            lastModified = opdsFeed.ofeLastModifiedHeader,
            etag = opdsFeed.ofeEtag,
        )
    )
}

