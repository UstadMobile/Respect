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
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.opds.adapters.OpdsFeedEntities
import world.respect.datalayer.db.opds.adapters.OpdsPublicationEntities
import world.respect.datalayer.db.opds.adapters.asEntities
import world.respect.datalayer.db.opds.adapters.asModel
import world.respect.datalayer.db.shared.adapters.asNetworkValidationInfo
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.networkvalidation.NetworkValidationInfo
import world.respect.datalayer.school.opds.OpdsDataSourceLocal
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

class OpdsDataSourceDb(
    private val respectSchoolDatabase: RespectSchoolDatabase,
    private val json: Json,
    private val uidNumberMapper: UidNumberMapper,
    private val primaryKeyGenerator: PrimaryKeyGenerator,
): OpdsDataSourceLocal {

    override val feedNetworkValidationHelper = object: BaseDataSourceValidationHelper {
        override suspend fun getValidationInfo(
            url: Url,
            requestHeaders: IHttpHeaders,
        ): NetworkValidationInfo? {
            return respectSchoolDatabase.getOpdsFeedEntityDao().getLastModifiedAndETag(
                uidNumberMapper(url.toString())
            )?.asNetworkValidationInfo()
        }
    }

    override val publicationNetworkValidationHelper = object: BaseDataSourceValidationHelper {
        override suspend fun getValidationInfo(
            url: Url,
            requestHeaders: IHttpHeaders,
        ): NetworkValidationInfo? {
            return respectSchoolDatabase.getOpdsPublicationEntityDao().getLastModifiedAndETag(
                uidNumberMapper(url.toString())
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
            uidNumberMapper = uidNumberMapper,
        ) ?: return

        respectSchoolDatabase.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val feedUid = uidNumberMapper(feedUrl.toString())
                respectSchoolDatabase.getOpdsFeedEntityDao().deleteByFeedUid(feedUid)
                respectSchoolDatabase.getOpdsFeedMetadataEntityDao().deleteByFeedUid(feedUid)
                respectSchoolDatabase.getLangMapEntityDao().deleteAllByFeedUid(feedUid)
                respectSchoolDatabase.getReadiumLinkEntityDao().deleteAllByFeedUid(feedUid)
                respectSchoolDatabase.getOpdsPublicationEntityDao().deleteAllByFeedUid(feedUid)
                respectSchoolDatabase.getOpdsGroupEntityDao().deleteByFeedUid(feedUid)

                respectSchoolDatabase.getOpdsFeedEntityDao().insertList(listOf(feedEntities.opdsFeed))
                respectSchoolDatabase.getOpdsFeedMetadataEntityDao().insertList(feedEntities.feedMetaData)
                respectSchoolDatabase.getLangMapEntityDao().insertAsync(feedEntities.langMapEntities)
                respectSchoolDatabase.getReadiumLinkEntityDao().insertList(feedEntities.linkEntities)
                respectSchoolDatabase.getOpdsPublicationEntityDao().insertList(feedEntities.publications)
                respectSchoolDatabase.getOpdsGroupEntityDao().insertList(feedEntities.groups)
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
            uidNumberMapper = uidNumberMapper,
            feedUid = 0,
            groupUid = 0,
            feedIndex = 0,
        )

        respectSchoolDatabase.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val oldPubUid = respectSchoolDatabase.getOpdsPublicationEntityDao().getUidByUrlHash(
                    uidNumberMapper(url.toString())
                )

                respectSchoolDatabase.getLangMapEntityDao().deleteByTableAndTopParentType(
                    lmeTopParentType = LangMapEntity.TopParentType.OPDS_PUBLICATION.id,
                    lmeEntityUid1 = oldPubUid,
                )
                respectSchoolDatabase.getReadiumLinkEntityDao().deleteAllByPublicationUid(oldPubUid)
                respectSchoolDatabase.getOpdsPublicationEntityDao().deleteByUid(oldPubUid)

                respectSchoolDatabase.getOpdsPublicationEntityDao().insertList(
                    listOf(publicationEntities.opdsPublicationEntity)
                )
                respectSchoolDatabase.getLangMapEntityDao().insertAsync(publicationEntities.langMapEntities)
                respectSchoolDatabase.getReadiumLinkEntityDao().insertList(publicationEntities.linkEntities)
            }
        }
    }

    override fun loadOpdsFeed(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return respectSchoolDatabase.getOpdsFeedEntityDao().findByUrlHashAsFlow(
            uidNumberMapper(url.toString())
        ).map { feedEntity ->
            respectSchoolDatabase.takeIf { feedEntity != null }?.useReaderConnection {
                feedEntity?.let {
                    OpdsFeedEntities(
                        opdsFeed = feedEntity,
                        feedMetaData = respectSchoolDatabase.getOpdsFeedMetadataEntityDao().findByFeedUid(
                            feedEntity.ofeUid),
                        langMapEntities = respectSchoolDatabase.getLangMapEntityDao().findAllByFeedUid(feedEntity.ofeUid),
                        linkEntities =respectSchoolDatabase.getReadiumLinkEntityDao().findAllByFeedUid(feedEntity.ofeUid),
                        publications = respectSchoolDatabase.getOpdsPublicationEntityDao().findByFeedUid(
                            feedEntity.ofeUid),
                        groups = respectSchoolDatabase.getOpdsGroupEntityDao().findByFeedUid(feedEntity.ofeUid),
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
        val urlHash = uidNumberMapper(url.toString())

        return respectSchoolDatabase.getOpdsPublicationEntityDao().findByUrlHashAsFlow(urlHash).map { entity ->
            entity?.let {
                OpdsPublicationEntities(
                    opdsPublicationEntity = entity,
                    langMapEntities = respectSchoolDatabase.getLangMapEntityDao().selectAllByTableAndEntityId(
                        lmeTopParentType = LangMapEntity.TopParentType.OPDS_PUBLICATION.id,
                        lmeEntityUid1 = entity.opeUid,
                        lmeEntityUid2 = 0
                    ),
                    linkEntities = respectSchoolDatabase.getReadiumLinkEntityDao().findAllByFeedUid(entity.opeUid)
                ).asModel(json)
            } ?: NoDataLoadedState.notFound()
        }
    }
}