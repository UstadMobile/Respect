package world.respect.datasource.compatibleapps

import kotlinx.coroutines.flow.Flow
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadState
import world.respect.datasource.compatibleapps.model.RespectAppManifest

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
    fun getApp(
        manifestUrl: String,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<RespectAppManifest>>

    /**
     * Load a list of apps that can be added to the launchpad
     *
     * The flow type is a little strange; because each url is itself loaded from a separate url,
     * that means that each item in the list has its own load state (including validation info)
     */
    fun getAddableApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>>

    fun getLaunchpadApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>>

    suspend fun addAppToLaunchpad(
        manifestUrl: String,
    )

    suspend fun removeAppFromLaunchpad(
        manifestUrl: String
    )

}
