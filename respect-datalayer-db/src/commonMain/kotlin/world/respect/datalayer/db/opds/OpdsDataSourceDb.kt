package world.respect.datalayer.db.opds

import androidx.room.Transactor
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import com.ustadmobile.ihttp.headers.IHttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.opds.adapters.OpdsFeedEntities
import world.respect.datalayer.db.opds.adapters.OpdsPublicationEntities
import world.respect.datalayer.db.opds.adapters.asEntities
import world.respect.datalayer.db.opds.adapters.asModel
import world.respect.datalayer.db.shared.adapters.asNetworkValidationInfo
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.networkvalidation.NetworkValidationInfo
import world.respect.datalayer.opds.OpdsDataSourceLocal
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libxxhash.XXStringHasher

class OpdsDataSourceDb(
    private val respectDatabase: RespectAppDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
    private val primaryKeyGenerator: PrimaryKeyGenerator = PrimaryKeyGenerator(
        RespectAppDatabase.TABLE_IDS
    ),
): OpdsDataSourceLocal {

    override val feedNetworkValidationHelper = object: BaseDataSourceValidationHelper {
        override suspend fun getValidationInfo(
            url: Url,
            requestHeaders: IHttpHeaders,
        ): NetworkValidationInfo? {
            return respectDatabase.getOpdsFeedEntityDao().getLastModifiedAndETag(
                xxStringHasher.hash(url.toString())
            )?.asNetworkValidationInfo()
        }
    }

    override val publicationNetworkValidationHelper = object: BaseDataSourceValidationHelper {
        override suspend fun getValidationInfo(
            url: Url,
            requestHeaders: IHttpHeaders,
        ): NetworkValidationInfo? {
            return respectDatabase.getOpdsPublicationEntityDao().getLastModifiedAndETag(
                xxStringHasher.hash(url.toString())
            )?.asNetworkValidationInfo()
        }
    }

    /**
     * Update the database with the given opdsfeed by converting it to entities. Delete any previous
     * entities associated with the given publication.
     */
    override suspend fun updateOpdsFeed(feed: DataReadyState<OpdsFeed>) {
        val feedUrl = feed.metaInfo.requireUrl()

        val feedEntities = feed.asEntities(
            json = json,
            primaryKeyGenerator = primaryKeyGenerator,
            xxStringHasher = xxStringHasher,
        ) ?: return

        respectDatabase.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val feedUid = xxStringHasher.hash(feedUrl.toString())
                respectDatabase.getOpdsFeedEntityDao().deleteByFeedUid(feedUid)
                respectDatabase.getOpdsFeedMetadataEntityDao().deleteByFeedUid(feedUid)
                respectDatabase.getLangMapEntityDao().deleteAllByFeedUid(feedUid)
                respectDatabase.getReadiumLinkEntityDao().deleteAllByFeedUid(feedUid)
                respectDatabase.getOpdsPublicationEntityDao().deleteAllByFeedUid(feedUid)
                respectDatabase.getOpdsGroupEntityDao().deleteByFeedUid(feedUid)

                respectDatabase.getOpdsFeedEntityDao().insertList(listOf(feedEntities.opdsFeed))
                respectDatabase.getOpdsFeedMetadataEntityDao().insertList(feedEntities.feedMetaData)
                respectDatabase.getLangMapEntityDao().insertAsync(feedEntities.langMapEntities)
                respectDatabase.getReadiumLinkEntityDao().insertList(feedEntities.linkEntities)
                respectDatabase.getOpdsPublicationEntityDao().insertList(feedEntities.publications)
                respectDatabase.getOpdsGroupEntityDao().insertList(feedEntities.groups)
            }
        }
    }

    override suspend fun updateOpdsPublication(publication: DataReadyState<OpdsPublication>) {
        val pubData = publication.data
        val url = publication.metaInfo.requireUrl()

        val publicationEntities = pubData.asEntities(
            dataLoadResult = publication,
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            xxStringHasher = xxStringHasher,
            feedUid = 0,
            groupUid = 0,
            feedIndex = 0,
        )

        respectDatabase.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val oldPubUid = respectDatabase.getOpdsPublicationEntityDao().getUidByUrlHash(
                    xxStringHasher.hash(url.toString())
                )

                respectDatabase.getLangMapEntityDao().deleteByTableAndTopParentType(
                    lmeTopParentType = LangMapEntity.TopParentType.OPDS_PUBLICATION.id,
                    lmeEntityUid1 = oldPubUid,
                )
                respectDatabase.getReadiumLinkEntityDao().deleteAllByPublicationUid(oldPubUid)
                respectDatabase.getOpdsPublicationEntityDao().deleteByUid(oldPubUid)

                respectDatabase.getOpdsPublicationEntityDao().insertList(
                    listOf(publicationEntities.opdsPublicationEntity)
                )
                respectDatabase.getLangMapEntityDao().insertAsync(publicationEntities.langMapEntities)
                respectDatabase.getReadiumLinkEntityDao().insertList(publicationEntities.linkEntities)
            }
        }
    }

    override fun loadOpdsFeed(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return respectDatabase.getOpdsFeedEntityDao().findByUrlHashAsFlow(
            xxStringHasher.hash(url.toString())
        ).map { feedEntity ->
            respectDatabase.takeIf { feedEntity != null }?.useReaderConnection { con ->
                feedEntity?.let {
                    OpdsFeedEntities(
                        opdsFeed = feedEntity,
                        feedMetaData = respectDatabase.getOpdsFeedMetadataEntityDao().findByFeedUid(
                            feedEntity.ofeUid),
                        langMapEntities = respectDatabase.getLangMapEntityDao().findAllByFeedUid(feedEntity.ofeUid),
                        linkEntities =respectDatabase.getReadiumLinkEntityDao().findAllByFeedUid(feedEntity.ofeUid),
                        publications = respectDatabase.getOpdsPublicationEntityDao().findByFeedUid(
                            feedEntity.ofeUid),
                        groups = respectDatabase.getOpdsGroupEntityDao().findByFeedUid(feedEntity.ofeUid),
                    ).asModel(json)
                }
            } ?: NoDataLoadedState.notFound()
        }
    }

    override fun loadOpdsPublication(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<OpdsPublication>> {
        val urlHash = xxStringHasher.hash(url.toString())

        return respectDatabase.getOpdsPublicationEntityDao().findByUrlHashAsFlow(urlHash).map { entity ->
            entity?.let {
                OpdsPublicationEntities(
                    opdsPublicationEntity = entity,
                    langMapEntities = respectDatabase.getLangMapEntityDao().selectAllByTableAndEntityId(
                        lmeTopParentType = LangMapEntity.ODPS_PUBLICATION_PARENT_ID,
                        lmeEntityUid1 = entity.opeUid,
                        lmeEntityUid2 = 0
                    ),
                    linkEntities = respectDatabase.getReadiumLinkEntityDao().findAllByFeedUid(entity.opeUid)
                ).asModel(json)
            } ?: NoDataLoadedState.notFound()
        }
    }
}