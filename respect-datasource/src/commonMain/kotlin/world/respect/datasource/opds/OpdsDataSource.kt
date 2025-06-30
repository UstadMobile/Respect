package world.respect.datasource.opds

import kotlinx.coroutines.flow.Flow
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadState
import world.respect.datasource.opds.model.OpdsFeed
import world.respect.datasource.opds.model.OpdsPublication

interface OpdsDataSource {

    /**
     * Load an OPDS Feed from a given URL
     */
    fun loadOpdsFeed(
        url: String,
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
        url: String,
        params: DataLoadParams,
        referrerUrl: String?,
        expectedPublicationId: String?,
    ): Flow<DataLoadState<OpdsPublication>>

}