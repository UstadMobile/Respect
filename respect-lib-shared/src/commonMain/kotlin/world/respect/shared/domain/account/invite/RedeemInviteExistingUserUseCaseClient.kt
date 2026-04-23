package world.respect.shared.domain.account.invite


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import world.respect.libutil.ext.appendEndpointSegments

class RedeemInviteExistingUserUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
) : RedeemInviteExistingUserUseCase {

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest,
        selectedChildGuid : String ?
    ) {
        return httpClient.post(
            schoolUrl.appendEndpointSegments("api/school/respect/invite/existingUserRedeem")
        ) {
            contentType(ContentType.Application.Json)
            parameter("selectedChildGuid", selectedChildGuid)
            setBody(redeemRequest)

        }.body()
    }

}