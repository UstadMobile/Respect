package world.respect.datasource.sqldelight

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import world.respect.datasource.RespectAppDataSourceLocal
import world.respect.datasource.compatibleapps.CompatibleAppsDataSourceLocal
import world.respect.datasource.opds.OpdsDataSource
import world.respect.datasource.sqldelight.compatibleapps.CompatibleAppsDataSourceSqld
import kotlin.coroutines.CoroutineContext

class RespectAppDataSourceSqld(
    private val respectDb: RespectDb,
    private val json: Json,
    private val coroutineContext: CoroutineContext = Dispatchers.IO,
): RespectAppDataSourceLocal {

    override val opdsDataSource: OpdsDataSource
        get() = TODO("Not yet implemented")

    override val compatibleAppsDataSource: CompatibleAppsDataSourceLocal by lazy {
        CompatibleAppsDataSourceSqld(
            queries = respectDb.compatibleAppEntityQueries,
            json = json,
            coroutineContext = coroutineContext,
        )
    }
}
