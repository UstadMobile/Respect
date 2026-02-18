package world.respect.datalayer.school.opds

import io.ktor.http.Url
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.lib.opds.model.OpdsFeed

/**
 * OpdsFeedDataSource: essentially handles Playlists, using the Opds standard.
 *
 * There is a key difference between an Opds DataSource and others: this DataSource can fetch data
 * from external, third party servers that are serving Opds feeds over HTTP, whereas other DataSources
 * only fetch data from a single upstream school server.
 *
 * Our general approach to data models (as per ModelWithTimes) requires a last modified time,
 * stored time, and uid for each entity. For OpdsFeed this is handled as follows:
 *  last-modified: OpdsFeedMetadata.modified
 *  stored: only kept on the database side
 *  uid: the OpdsFeed url (as per the self link in the model). Playlists created on school are in
 *  the form of school-url/playlists/uuid
 *
 * Client side:
 *  The remote datasource will fetch from a server as specified by the URL, which may, or may not,
 *  be the school URL.
 *
 * Server side:
 * The server is expected to 'crawl' the school's available OpdsFeeds such that it can handle
 * search queries.
 *
 * OpdsFeeds Playlists are NOT modified or transformed by any server: they are copied. E.g. if a
 * feed from an app developer has poor metadata, the server will NOT transform the original feed. The
 * school admin or school teachers can copy the app's feed, and then modify it.
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
     * Load an OPDS Feed from a given URL : essentially the same as getByUid for other data types.
     */
    fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ):  Flow<DataLoadState<OpdsFeed>>

}