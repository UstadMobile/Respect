package world.respect.shared.domain.createlink

import io.ktor.http.Url
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.util.RespectUrlComponents

class CreateLinkUseCase(
    private val schoolUrl: Url
) {
    operator fun invoke(
        code: String
    ): String {
        return RespectUrlComponents(
            schoolUrl.toString(), SignupScreen.toString(),
            "inviteCode=${code}"
        ).fullUrl()
    }

}