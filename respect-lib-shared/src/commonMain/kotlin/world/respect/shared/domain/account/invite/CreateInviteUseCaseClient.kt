package world.respect.shared.domain.account.invite

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class CreateInviteUseCaseClient(
    override val schoolUrl: Url,
    private val httpClient: HttpClient,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
) : CreateInviteUseCase, SchoolUrlBasedDataSource {

    override suspend fun invoke(invite: Invite): String {
        return httpClient.post(
            URLBuilder(respectEndpointUrl("invite/create")).build()
        ) {
            contentType(ContentType.Application.Json)
            setBody(invite)
        }.body()
    }
}
