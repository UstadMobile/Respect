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
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.http.ext.xapiEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.AssignmentResult
import world.respect.lib.xapi.model.XAPI_RESULT_EXTENSION_PROGRESS
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.lib.xapi.resources.setXapiGetStatementsParams
import kotlin.text.get
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
        TODO("Not yet implemented")
    }

    override fun getAssignmentCompletions(
        listParams: GetStatementParams
    ): Flow<List<AssignmentResult>> {
        return getAsFlow(
            listParams = listParams,
            dataLoadParams = DataLoadParams()
        ).map { state ->
            state.dataOrNull()?.statements?.mapNotNull { statement ->
                val actor = statement.actor
                val personUid = when (actor) {
                    is XapiAgent -> actor.account?.name ?: actor.mbox ?: actor.openid ?: actor.name
                    else -> null
                } ?: return@mapNotNull null
                val personName = actor.name
                val activityId = (statement.`object` as? XapiActivity)?.id ?: return@mapNotNull null
                val result = statement.result
                AssignmentResult(
                    personUid = personUid,
                    personName = personName,
                    activityId = activityId,
                    completion = result?.completion,
                    success = result?.success,
                    scoreScaled = result?.score?.scaled,
                    progress = result?.extensions?.get(XAPI_RESULT_EXTENSION_PROGRESS)?.toString()?.toIntOrNull()
                )
            } ?: emptyList()
        }
    }



    override suspend fun getLastStoredTimestampForActivity(activityId: String): Long? {
        TODO("Not yet implemented")
    }
}