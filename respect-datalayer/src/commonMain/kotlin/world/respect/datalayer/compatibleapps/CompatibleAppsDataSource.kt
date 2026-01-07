package world.respect.datalayer.compatibleapps

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest

/**
 * The RespectAppManifest data structure itself does not contain validation information (e.g. the
 * last modified header, or etag). It also does not contain the URL. The same will be true when
 * loading OPDS over http.
 *
 * The DataLoadState is used to hold this information.
 */
interface CompatibleAppsDataSource {

    /**
     * Load a specific app manifest
     */
    fun getAppAsFlow(
        manifestUrl: Url,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<RespectAppManifest>>

    suspend fun getApp(
        manifestUrl: Url,
        loadParams: DataLoadParams
    ): DataLoadState<RespectAppManifest>

    /**
     * Load a list of apps that can be added to the launchpad
     *
     * The flow type is a little strange; because each url is itself loaded from a separate url,
     * that means that each item in the list has its own load state (including validation info)
     */
    fun getAddableApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>>

}
