package world.respect.shared.domain.geticonforxapiactivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.ext.webPubManifestAsUrlOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement

class GetPublicationForXapiActivityUseCase(
    private val opdsPublicationDataSource: OpdsPublicationDataSource,
) {


    operator fun invoke(
        activity: XapiActivity
    ) : Flow<DataLoadState<OpdsPublication>> {
        return activity.definition?.webPubManifestAsUrlOrNull()?.let { publicationUrl ->
            opdsPublicationDataSource.getByUrlAsFlow(
                url = publicationUrl,
                params = DataLoadParams(),
                referrerUrl = null,
                expectedPublicationId = null,
            )
        } ?: flowOf(NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND))
    }

    operator fun invoke(
        statement: XapiStatement
    ): Flow<DataLoadState<OpdsPublication>>  {
        return (statement.`object` as? XapiActivity)?.let { invoke(it) }
            ?: flowOf(NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND))
    }


}