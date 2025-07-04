package world.respect.datasource.db

import kotlinx.serialization.json.Json
import world.respect.datasource.RespectAppDataSourceLocal
import world.respect.datasource.compatibleapps.CompatibleAppsDataSourceLocal
import world.respect.datasource.db.compatibleapps.CompatibleAppDataSourceDb
import world.respect.datasource.opds.OpdsDataSource
import world.respect.libxxhash.XXStringHasher

class RespectAppDataSourceDb(
    private val respectDatabase: RespectDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
): RespectAppDataSourceLocal {

    override val compatibleAppsDataSource: CompatibleAppsDataSourceLocal by lazy {
        CompatibleAppDataSourceDb(respectDatabase, json, xxStringHasher)
    }

    override val opdsDataSource: OpdsDataSource
        get() = TODO("Not yet implemented")
}