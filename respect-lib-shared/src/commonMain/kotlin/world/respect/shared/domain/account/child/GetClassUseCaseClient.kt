package world.respect.shared.domain.account.child

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class GetClassUseCaseClient (
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
) : GetClassUseCase , SchoolUrlBasedDataSource {


    override suspend fun invoke(classUid: String): String {
        return httpClient.post(
            URLBuilder(respectEndpointUrl("class/name"))
                .apply {
                    parameters.append("classUid", classUid)
                }.build()
        ).body()
    }
}