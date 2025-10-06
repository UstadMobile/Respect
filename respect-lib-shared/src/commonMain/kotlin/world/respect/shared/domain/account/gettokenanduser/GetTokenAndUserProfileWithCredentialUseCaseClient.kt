package world.respect.shared.domain.account.gettokenanduser

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import world.respect.credentials.passkey.RespectCredential
import world.respect.shared.domain.account.AuthResponse
import world.respect.datalayer.school.model.DeviceInfo
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase

class GetTokenAndUserProfileWithCredentialUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
): GetTokenAndUserProfileWithCredentialUseCase {

    override suspend fun invoke(
        credential: RespectCredential,
        deviceInfo: DeviceInfo?,
    ): AuthResponse {
        return httpClient.post {
            url {
                takeFrom(schoolUrl)
                appendPathSegments("api/school/respect/auth/auth-with-password")
            }
            header(
                key = DeviceInfo.Companion.HEADER_NAME,
                value = (deviceInfo ?: getDeviceInfoUseCase()).toHeaderLine(),
            )

            contentType(ContentType.Application.Json)
            setBody(credential)
        }.body()
    }
}