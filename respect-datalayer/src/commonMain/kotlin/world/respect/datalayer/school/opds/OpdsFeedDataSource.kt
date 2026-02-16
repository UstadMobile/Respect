package world.respect.datalayer.school.opds

import io.ktor.util.StringValues
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.lib.opds.model.OpdsFeed

/**
 * OpdsFeedDataSource: essentially handles Playlists, using the Opds standard. Our general approach
 * to data models (as per ModelWithTimes) requires a last modified time, stored time, and uid for
 * each entity. For OpdsFeed this is handled as follows:
 *  last-modified: OpdsFeedMetadata.modified
 *  stored: only kept on the database side
 *  uid: the OpdsFeed url. Playlists created on school are in the form of school-url/playlists/uuid
 *
 * Client side: app uses 'normal' approach - local database and remote repository. All upstream
 * requests go to the school server, regardless of url
 *
 * Server side: server uses a repository. For any specified url that is not on the school server
 * itself, it will attempt to fetch from the upstream url.
 *
 * This approach allows the server to modify / process OpdsFeeds: e.g. to include additional
 * metadata that was not otherwise present, to mitigate the effect of a compatible app's server
 * being down, add download length to manifests, etc.
 */
interface OpdsFeedDataSource : WritableDataSource<OpdsFeed>{

    /**
     * common.guid is the Url of the OpdsFeed.
     */
    data class GetListParams(
        val common: GetListCommonParams
    ) {
        companion object {

            fun fromParams(params: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params)
                )
            }
        }
    }

    /**
     *
     */
    suspend fun list(
        loadParams: DataLoadParams = DataLoadParams(),
        listParams: GetListParams
    ): List<OpdsFeed>

}