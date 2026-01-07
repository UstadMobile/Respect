package world.respect.datalayer.school.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsPublication

interface OpdsDataSource {

    /**
     * Load an OPDS Feed from a given URL
     */
    fun loadOpdsFeed(
        url: Url,
        params: DataLoadParams
    ):  Flow<DataLoadState<OpdsFeed>>

    /**
     *
     * @param url
     * @param params
     * @param referrerUrl where a publication is being loaded based on following a link from an
     *        opds feed, providing the URL and publicationId of the feed can be used to load a first
     *        version from the cache
     * @param expectedPublicationId where a publication is being loaded based on following a link from an
     *        opds feed, providing the URL and publicationId of the feed can be used to load a first
     *        version from the cache
     */
    fun loadOpdsPublication(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?,
    ): Flow<DataLoadState<OpdsPublication>>

}