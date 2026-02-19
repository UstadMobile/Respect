package world.respect.datalayer.db.opds

import androidx.room.Transactor
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
import world.respect.datalayer.db.opds.adapters.OpdsPublicationEntities
import world.respect.datalayer.db.opds.adapters.asEntities
import world.respect.datalayer.db.opds.adapters.asModel
import world.respect.datalayer.db.shared.adapters.asNetworkValidationInfo
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.networkvalidation.NetworkValidationInfo
import world.respect.datalayer.school.opds.OpdsDataSourceLocal
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