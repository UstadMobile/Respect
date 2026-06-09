package world.respect.shared.domain.account.invite


import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.useTokenProvider
import world.respect.libutil.ext.appendEndpointSegments

class RedeemInviteExistingUserUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
    private val authTokenProvider: AuthTokenProvider,
    ) : RedeemInviteExistingUserUseCase {

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest,
        selectedChildGuid: String?
    ) {
        val response = httpClient.post(
            schoolUrl.appendEndpointSegments("api/school/respect/existingUserRedeem")
        ) {
            contentType(ContentType.Application.Json)
            useTokenProvider(authTokenProvider)
            parameter("selectedChildGuid", selectedChildGuid)
            setBody(redeemRequest)
        }

        if (!response.status.isSuccess()) {
            throw IllegalArgumentException(
                response.bodyAsText().ifBlank { "Something went wrong with invite" }
            ) as Throwable
        }
    }

}