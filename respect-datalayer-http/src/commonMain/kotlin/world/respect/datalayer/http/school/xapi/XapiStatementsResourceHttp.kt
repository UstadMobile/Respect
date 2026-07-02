package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.toDataLoadState
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.http.ext.xapiEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import kotlin.uuid.Uuid

class XapiStatementsResourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val json: Json,
): XapiStatementsResource, SchoolUrlBasedDataSource {

    private suspend fun GetStatementParams.urlWithParams(): Url {
        return URLBuilder(xapiEndpointUrl(XapiStatementsResource.ENDPOINT_NAME)).also {
            it.parameters.appendAll(this.toParameters(json))
        }.build()
    }

    override suspend fun post(list: List<XapiStatement>): DataLoadState<List<Uuid>> {
        return httpClient.post(
            url = xapiEndpointUrl(XapiStatementsResource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)

            contentType(ContentType.Application.Json)
            setBody(list)
        }.toDataLoadState(typeInfo<List<Uuid>>())
    }

    override suspend fun get(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiStatementResult> {

        return httpClient.getAsDataLoadState<XapiStatementResult>(
            url = listParams.urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
        }
    }

    override fun getAsFlow(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        return httpClient.getDataLoadResultAsFlow<XapiStatementResult>(
            urlFn = {
                listParams.urlWithParams()
            },
            dataLoadParams = dataLoadParams,
        ) {
            useTokenProvider(tokenProvider)
        }
    }

    override fun getAssignmentProgress(
        activityId: String,
        filterByAssigneeAgent: XapiAgent?
    ): Flow<DataLoadState<AssignmentAndProgress>> {
        throw IllegalStateException("GetAssignmentResults over HTTP is not supported")
    }

    override fun getAssignmentListAsFlow(dataLoadParams: DataLoadParams, studentAgent: XapiAgent?): Flow<DataLoadState<List<AssignmentSummary>>> {
        throw IllegalStateException("GetAssignmentResults over HTTP is not supported")
    }
}