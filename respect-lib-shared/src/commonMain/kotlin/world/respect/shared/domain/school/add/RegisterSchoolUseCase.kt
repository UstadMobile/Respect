package world.respect.shared.domain.school.add

import io.ktor.http.Url
import kotlinx.serialization.Serializable

interface RegisterSchoolUseCase {

    @Serializable
    data class RegisterSchoolRequest(
        val schoolName: String,
        val schoolUrl: String,
    )

    @Serializable
    data class RegisterSchoolResponse(
        val schoolUrl: Url,
        val redirectUrl: Url
    )

    suspend operator fun invoke(
        request: RegisterSchoolRequest
    ): RegisterSchoolResponse

}