package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.parameters
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.http.ext.xapiEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.AssignmentResult
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.lib.xapi.resources.setXapiGetStatementsParams
import kotlin.uuid.Uuid

class XapiStatementsResourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val json: Json,
): XapiStatementsResource, SchoolUrlBasedDataSource {

    override suspend fun post(list: List<XapiStatement>): List<Uuid> {
        val response = httpClient.post(
            url = xapiEndpointUrl(XapiStatementsResource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)

            contentType(ContentType.Application.Json)
            setBody(list)
        }

        return response.body()
    }

    override suspend fun get(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiStatementResult> {

        return httpClient.getAsDataLoadState(
            url = xapiEndpointUrl(XapiStatementsResource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            parameters {
                setXapiGetStatementsParams(listParams, json)
            }
        }
    }

    override fun getAsFlow(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        return httpClient.getDataLoadResultAsFlow(
            urlFn = {
                xapiEndpointUrl(XapiStatementsResource.ENDPOINT_NAME)
            },
            dataLoadParams = dataLoadParams,
        ) {
            useTokenProvider(tokenProvider)

            parameters {
                setXapiGetStatementsParams(listParams, json)
            }
        }
    }

    override fun getAssignmentResult(
        assignmentActivityId: String,
    ): Flow<List<AssignmentResult>> {
        throw UnsupportedOperationException("getAssignmentResult on http NOT supported - this operation is only supported on local database source")
    }

    override fun getAssignmentSummaries(): Flow<List<AssignmentSummary>> {
        throw UnsupportedOperationException("getAssignmentSummaries on http NOT supported - this operation is only supported on local database source")
    }

    override suspend fun getLastStoredTimestampForActivity(activityId: String): Long? {
        throw UnsupportedOperationException("getLastStoredTimestampForActivity on http NOT supported - this operation is only supported on local database source")
    }
}
