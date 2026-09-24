package world.respect.shared.domain.catalog.getopdsfeedforxapiactivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.xapi.ext.opdsCollectionLinkAsUrlOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement

/**
 * Resolve the OpdsFeed (collection) linked to a pin-collection xAPI statement (or its activity)
 * as per the Collections Listing recipe: README_COLLECTIONS_LISTING_RECIPE.md . The
 * opds-collection-link extension provides the url that can be used to retrieve the OpdsFeed via
 * the xAPI Activity Profile Resource (see README_OPDS_COLLECTIONS_ACTIVITY_PROFILE.md).
 */
class GetOpdsFeedForXapiActivityUseCase(
    private val opdsFeedDataSource: OpdsFeedDataSource,
) {

    operator fun invoke(
        activity: XapiActivity
    ): Flow<DataLoadState<OpdsFeed>> {
        return activity.definition?.opdsCollectionLinkAsUrlOrNull()?.let { collectionLinkUrl ->
            opdsFeedDataSource.getByUrlAsFlow(
                url = collectionLinkUrl,
                params = DataLoadParams(),
            )
        } ?: flowOf(NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND))
    }

    operator fun invoke(
        statement: XapiStatement
    ): Flow<DataLoadState<OpdsFeed>> {
        return (statement.`object` as? XapiActivity)?.let { invoke(it) }
            ?: flowOf(NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND))
    }

}
