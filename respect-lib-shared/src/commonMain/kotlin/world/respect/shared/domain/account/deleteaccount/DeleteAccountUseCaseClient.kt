package world.respect.shared.domain.account.deleteaccount

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class DeleteAccountUseCaseClient(
    private val httpClient: HttpClient,
    override val schoolUrl: Url,
    private val tokenProvider: AuthTokenProvider,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource
) : DeleteAccountUseCase, SchoolUrlBasedDataSource {

    override suspend fun invoke(guid: String): Boolean {
        return try {
            val response: HttpResponse = httpClient.post(
                respectEndpointUrl("person/delete")
            ) {
                useTokenProvider(tokenProvider)
            }

            val success = response.status == HttpStatusCode.OK ||
                    response.status == HttpStatusCode.NoContent

            return success

        } catch (e: Exception) {
            Napier.e("DeleteAccountUseCase: deleteByGuid($guid) failed: ${e.message}", e)
            false
        }
    }
}
