package world.respect.datalayer.http.school.xapi

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.bodyAsXapiDocument
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.toDataLoadState
import world.respect.datalayer.ext.toXapiDocumentDataLoadState
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.http.ext.xapiEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.resources.XapiActivityProfileResource

class XapiActivityProfileResourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    @Suppress("unused")
    private val json: Json,
): XapiActivityProfileResource, SchoolUrlBasedDataSource {

    private suspend fun XapiActivityProfileResource.MultiDocParams.urlWithParams(): Url {
        return URLBuilder(xapiEndpointUrl("activities/${XapiActivityProfileResource.ENDPOINT_NAME}")).also {
            it.parameters.appendAll(this.toParameters())
        }.build()
    }

    private suspend fun XapiActivityProfileResource.SingleDocumentParams.urlWithParams(): Url {
        return URLBuilder(xapiEndpointUrl("activities/${XapiActivityProfileResource.ENDPOINT_NAME}")).also {
            it.parameters.appendAll(this.toParameters())
        }.build()
    }

    override suspend fun getMultipleDocuments(
        params: XapiActivityProfileResource.MultiDocParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<List<String>> {
        return httpClient.getAsDataLoadState<List<String>>(
            url = params.urlWithParams(),
        ) {
            useTokenProvider(tokenProvider)
        }
    }

    override suspend fun get(
        params: XapiActivityProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiDocument> {
        return httpClient.getAsDataLoadState(
            url = params.urlWithParams(),
            bodyAdapter = {
                it.bodyAsXapiDocument()
            }
        ) {
            useTokenProvider(tokenProvider)
        }
    }

    override suspend fun post(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        httpClient.post(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.parse(document.type))
            setBody(document.contentsAsByteArray())
        }
    }

    override suspend fun put(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        httpClient.put(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.parse(document.type))
            setBody(document.contentsAsByteArray())
        }
    }

    override suspend fun delete(params: XapiActivityProfileResource.SingleDocumentParams) {
        httpClient.delete(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
        }
    }
}