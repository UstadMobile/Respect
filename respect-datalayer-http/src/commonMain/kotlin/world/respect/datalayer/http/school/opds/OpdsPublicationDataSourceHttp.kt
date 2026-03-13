package world.respect.datalayer.http.school.opds

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.map
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.datalayer.school.opds.ext.asOpdsPublication
import world.respect.lib.opds.model.OpdsPublication

class OpdsPublicationDataSourceHttp(
    private val httpClient: HttpClient,
    private val publicationValidationHelper: BaseDataSourceValidationHelper? = null,
    private val json: Json,
) : OpdsPublicationDataSource {

    /**
     * RespectAppManifest (now deprecated in favor of using OpdsPublication)
     */
    private fun DataLoadState<JsonElement>.asPublicationIfRespectAppManifest(

    ): DataLoadState<OpdsPublication> {
        return this.map { element ->
            if(element is JsonObject && element.containsKey("defaultLaunchUri")) {
                json.decodeFromJsonElement(
                    RespectAppManifest.serializer(), element
                ).asOpdsPublication()
            }else {
                json.decodeFromJsonElement(OpdsPublication.serializer(), element)
            }
        }
    }

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<OpdsPublication>> {
        return httpClient.getDataLoadResultAsFlow<JsonElement>(
            urlFn = { url },
            dataLoadParams = params,
            validationHelper = publicationValidationHelper,
        ).map { dataLoadResult ->
            dataLoadResult.asPublicationIfRespectAppManifest()
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): DataLoadState<OpdsPublication> {
        return httpClient.getAsDataLoadState<JsonElement>(
            url = url,
            validationHelper = publicationValidationHelper,
        ).asPublicationIfRespectAppManifest()
    }
}
