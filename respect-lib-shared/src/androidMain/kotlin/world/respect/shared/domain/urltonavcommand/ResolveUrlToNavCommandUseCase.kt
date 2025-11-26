package world.respect.shared.domain.urltonavcommand

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.encodedPath
import world.respect.shared.domain.createlink.CreateLinkUseCase.Companion.QUERY_PARAM
import world.respect.shared.navigation.Acknowledgement
import world.respect.shared.navigation.NavCommand

class ResolveUrlToNavCommandUseCase {

    operator fun invoke(url: Url): NavCommand? {
        val inviteCode = url.parameters[QUERY_PARAM] ?: return null
        val schoolUrl = extractBaseUrl(url)

        return NavCommand.Navigate(
            destination = Acknowledgement.create(
                schoolUrl = schoolUrl,
                inviteCode = inviteCode
            ), clearBackStack = false
        )
    }

    private fun extractBaseUrl(url: Url): Url {
        return URLBuilder().apply {
            protocol = url.protocol
            host = url.host
            port = url.port
            encodedPath = "/"
        }.build()
    }
}





