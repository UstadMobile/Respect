package world.respect.shared.domain.createlink

import io.ktor.http.Url

class CreateLinkUseCase(
    private val schoolUrl: Url
) {
    operator fun invoke(
        code: String
    ): String {
        val base = schoolUrl.toString().trimEnd('/')
        return "$base/$PATH?$QUERY_PARAM=$code"
    }

    companion object {
        const val PATH = "invite"
        const val QUERY_PARAM = "inviteCode"
    }
}