package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class InviteDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
) : InviteDataSource, SchoolUrlBasedDataSource {

    private suspend fun InviteDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(InviteDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
            }
            .build()
    }


    override suspend fun store(list: List<Invite>) {
        httpClient.post(
            url = respectEndpointUrl(InviteDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}
