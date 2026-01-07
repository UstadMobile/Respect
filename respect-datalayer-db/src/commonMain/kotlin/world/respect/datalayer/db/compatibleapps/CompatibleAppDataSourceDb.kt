package world.respect.datalayer.db.compatibleapps

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.compatibleapps.CompatibleAppsDataSourceLocal
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.compatibleapps.adapters.asEntities
import world.respect.datalayer.db.compatibleapps.adapters.asModel
import world.respect.libxxhash.XXStringHasher
import com.ustadmobile.ihttp.headers.IHttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.combine
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.compatibleapps.adapters.combineWithLangMaps
import world.respect.datalayer.db.shared.adapters.asNetworkValidationInfo
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.networkvalidation.NetworkValidationInfo

class CompatibleAppDataSourceDb(
    private val respectDb: RespectAppDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
): CompatibleAppsDataSourceLocal, BaseDataSourceValidationHelper {

    override suspend fun getValidationInfo(
        url: Url,
        requestHeaders: IHttpHeaders,
    ): NetworkValidationInfo? {
        return respectDb.getCompatibleAppEntityDao().getNetworkValidationInfo(
            xxStringHasher.hash(url.toString())
        )?.asNetworkValidationInfo()
    }

    override suspend fun upsertCompatibleApps(apps: List<DataLoadState<RespectAppManifest>>) {
        val entities = apps.mapNotNull {
            (it as? DataReadyState)?.asEntities(json, xxStringHasher)
        }

//        respectDb.useWriterConnection { con ->
//            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
//                respectDb.getCompatibleAppEntityDao().upsert(
//                    entities.map { it.compatibleAppEntity }
//                )
//
//                entities.forEach {
//                    respectDb.getLangMapEntityDao().deleteByTableAndTopParentType(
//                        lmeTopParentType = LangMapEntity.TopParentType.RESPECT_MANIFEST.id,
//                        lmeEntityUid1 = it.compatibleAppEntity.caeUid,
//                    )
//                }
//
//                respectDb.getLangMapEntityDao().insertAsync(
//                    entities.flatMap { it.langMapEntities }
//                )
//            }
//        }
    }

    override suspend fun getApp(
        manifestUrl: Url,
        loadParams: DataLoadParams
    ) : DataLoadState<RespectAppManifest> {
        val caeUid = xxStringHasher.hash(manifestUrl.toString())
        TODO()
//        val appEntity =  respectDb.getCompatibleAppEntityDao().selectByUid(caeUid)
//        val langMapEntities = respectDb.getLangMapEntityDao().selectAllByTableAndEntityId(
//            lmeTopParentType = LangMapEntity.TopParentType.RESPECT_MANIFEST.id,
//            lmeEntityUid1 = caeUid,
//            lmeEntityUid2 = 0
//        )
//
//        return appEntity?.asModel(langMapEntities, json) ?: NoDataLoadedState.notFound()
    }

    override fun getAppAsFlow(
        manifestUrl: Url,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<RespectAppManifest>> {
        val caeUid = xxStringHasher.hash(manifestUrl.toString())
        TODO()
//        return respectDb.getCompatibleAppEntityDao().selectByUidAsFlow(
//            caeUid
//        ).combine(
//            respectDb.getLangMapEntityDao().selectAllByTableAndEntityIdAsFlow(
//                lmeTopParentTypeId = LangMapEntity.TopParentType.RESPECT_MANIFEST.id,
//                lmeEntityUid1 = caeUid,
//                lmeEntityUid2 = 0
//            )
//        ) { compatibleAppEntity, langMapEntities ->
//            compatibleAppEntity?.asModel(langMapEntities, json) ?: NoDataLoadedState.notFound()
//        }
    }

    override fun getAddableApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>> {
        val appEntities = respectDb.getCompatibleAppEntityDao().selectAllAsFlow()
        TODO()
//        val langmaps = respectDb.getLangMapEntityDao().selectAllByTopParentType(
//            LangMapEntity.TopParentType.RESPECT_MANIFEST.id
//        )
//        return appEntities.combineWithLangMaps(langmaps, json)
    }

}