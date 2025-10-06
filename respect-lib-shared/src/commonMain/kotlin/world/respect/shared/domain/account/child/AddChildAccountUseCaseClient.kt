package world.respect.shared.domain.account.child

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest

class AddChildAccountUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
    ) : AddChildAccountUseCase {

        override suspend fun invoke(personInfo: RespectRedeemInviteRequest.PersonInfo,parentUsername:String) {
            return httpClient.post(
                schoolUrl.appendEndpointSegments("api/school/respect/addchild")
            ) {
                contentType(ContentType.Application.Json)
                parameter("parentUsername",parentUsername)
                setBody(personInfo)
            }.body()
        }

    }