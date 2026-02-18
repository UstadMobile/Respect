package world.respect.datalayer.db.opds

import androidx.room.Transactor
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.opds.adapters.OpdsFeedEntities
import world.respect.datalayer.db.opds.adapters.asEntities
import world.respect.datalayer.db.opds.adapters.asModel
import world.respect.datalayer.db.opds.entities.OpdsFeedEntity
import world.respect.datalayer.school.opds.ext.requireSelfUrl
import world.respect.datalayer.school.opds.OpdsFeedDataSourceLocal
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import kotlin.time.Clock

class OpdsFeedDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json,
    private val primaryKeyGenerator: PrimaryKeyGenerator,
) : OpdsFeedDataSourceLocal{

    private suspend fun OpdsFeedEntity.loadModel(): OpdsFeed {
        return OpdsFeedEntities(
            opdsFeed = this,
            feedMetaData = schoolDb.getOpdsFeedMetadataEntityDao().findByFeedUid(this.ofeUid),
            langMapEntities = schoolDb.getLangMapEntityDao().findAllByFeedUid(this.ofeUid),
            linkEntities =schoolDb.getReadiumLinkEntityDao().findAllByFeedUid(this.ofeUid),
            publications = schoolDb.getOpdsPublicationEntityDao().findByFeedUid(
                this.ofeUid),
            groups = schoolDb.getOpdsGroupEntityDao().findByFeedUid(this.ofeUid),
        ).asModel(json)
    }

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return schoolDb.getOpdsFeedEntityDao().findByUrlHashAsFlow(
            uidNumberMapper(url.toString())
        ).map { feedEntity ->
            schoolDb.takeIf { feedEntity != null }?.useReaderConnection {
                feedEntity?.loadModel()?.let { opdsFeed ->
                    DataReadyState(
                        data = opdsFeed
                    )
                }
            } ?: NoDataLoadedState.notFound()
        }
    }

    private suspend fun doUpsertOpdsFeed(
        opdsFeed: OpdsFeed
    ) {
        val feedEntities = opdsFeed.asEntities(
            json = json,
            primaryKeyGenerator = primaryKeyGenerator,
            uidNumberMapper = uidNumberMapper,
        )
        val feedUrl = opdsFeed.requireSelfUrl()

        val feedUid = uidNumberMapper(feedUrl.toString())
        schoolDb.getOpdsFeedEntityDao().deleteByFeedUid(feedUid)
        schoolDb.getOpdsFeedMetadataEntityDao().deleteByFeedUid(feedUid)
        schoolDb.getLangMapEntityDao().deleteAllByFeedUid(feedUid)
        schoolDb.getReadiumLinkEntityDao().deleteAllByFeedUid(feedUid)
        schoolDb.getOpdsPublicationEntityDao().deleteAllByFeedUid(feedUid)
        schoolDb.getOpdsGroupEntityDao().deleteByFeedUid(feedUid)

        schoolDb.getOpdsFeedEntityDao().insertList(
            listOf(feedEntities.opdsFeed.copy(ofeStored = Clock.System.now()))
        )
        schoolDb.getOpdsFeedMetadataEntityDao().insertList(feedEntities.feedMetaData)
        schoolDb.getLangMapEntityDao().insertAsync(feedEntities.langMapEntities)
        schoolDb.getReadiumLinkEntityDao().insertList(feedEntities.linkEntities)
        schoolDb.getOpdsPublicationEntityDao().insertList(feedEntities.publications)
        schoolDb.getOpdsGroupEntityDao().insertList(feedEntities.groups)
    }

    override suspend fun store(list: List<OpdsFeed>) {
        //TODO: throw illegal argument exception if anything on list is not for this school url
        //TODO: run permission check to see if user is allowed to save/write this feed

        val timeNow = Clock.System.now()

        list.forEach { feed ->
            doUpsertOpdsFeed(
                opdsFeed = feed.copy(
                    metadata = feed.metadata.copy(modified = timeNow)
                )
            )
        }
    }

    override suspend fun updateLocal(
        list: List<OpdsFeed>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { feed ->
                    //TODO: check last modified before doing insert.

                    doUpsertOpdsFeed(feed)
                }
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<OpdsFeed> {
        return schoolDb.useReaderConnection {
            schoolDb.getOpdsFeedEntityDao().findByUrlHashList(
                urlHashes = uids.map { uidNumberMapper(it) }
            ).map { it.loadModel() }
        }
    }
}