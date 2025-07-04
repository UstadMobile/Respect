package world.respect.datasource.db.compatibleapps

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.Json
import world.respect.datasource.DataLoadMetaInfo
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.DataLoadState
import world.respect.datasource.LoadingStatus
import world.respect.datasource.compatibleapps.CompatibleAppsDataSourceLocal
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.db.RespectDatabase
import world.respect.datasource.db.adapters.asCompatibleAppEntities
import world.respect.datasource.db.adapters.asRespectManifestLoadResult
import world.respect.libxxhash.XXStringHasher
import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.combine
import world.respect.datasource.db.entities.CompatibleAppEntity
import world.respect.datasource.db.entities.composites.CompatibleAppEntities

class CompatibleAppDataSourceDb(
    private val respectDb: RespectDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
): CompatibleAppsDataSourceLocal {

    override suspend fun upsertCompatibleApps(apps: List<DataLoadResult<RespectAppManifest>>) {
        val entities = apps.mapNotNull { it.asCompatibleAppEntities(json, xxStringHasher) }

        respectDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                respectDb.getCompatibleAppEntityDao().upsert(
                    entities.map { it.compatibleAppEntity }
                )

                entities.forEach {
                    respectDb.getLangMapEntityDao().deleteByTableAndEntityUid(
                        lmeTableId = CompatibleAppEntity.TABLE_ID,
                        lmeEntityUid1 = it.compatibleAppEntity.caeUid,
                    )
                }

                respectDb.getLangMapEntityDao().insertAsync(
                    entities.flatMap { it.langMapEntities }
                )
            }
        }
    }

    override fun getApp(
        manifestUrl: String,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<RespectAppManifest>> {
        return emptyFlow()
    }

    override fun getAddableApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>> {
        val appEntities = respectDb.getCompatibleAppEntityDao().selectAllAsFlow()
        val langmaps = respectDb.getLangMapEntityDao().selectAllByTableId(CompatibleAppEntity.TABLE_ID)

        return appEntities.combine(langmaps) { appEntities, langmaps ->
            DataLoadResult(
                data = appEntities.map { appEntity ->
                    CompatibleAppEntities(
                        compatibleAppEntity = appEntity,
                        langMapEntities = langmaps.filter { it.lmeEntityUid1 == appEntity.caeUid }
                    ).asRespectManifestLoadResult(json)
                },
                metaInfo = DataLoadMetaInfo(
                    status = LoadingStatus.LOADED,
                    lastModified = appEntities.maxOfOrNull { it.caeLastModified } ?: 0,
                    etag = null,
                    url = null,
                )
            )
        }
    }

    override fun getLaunchpadApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>> {
        return emptyFlow()
    }

    override suspend fun addAppToLaunchpad(manifestUrl: String) {
        //Do nothing yet
    }

    override suspend fun removeAppFromLaunchpad(manifestUrl: String) {
        //Do nothing yet
    }
}