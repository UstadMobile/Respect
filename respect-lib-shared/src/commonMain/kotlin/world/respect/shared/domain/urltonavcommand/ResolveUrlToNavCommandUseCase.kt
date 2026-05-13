package world.respect.shared.domain.urltonavcommand

import io.ktor.http.Url
import world.respect.libutil.ext.schoolUrlOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.createlink.CreateInviteLinkUseCase
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SelectAccount

/**
 * Given a Url (that may have come from a deep link, scanned as a qr code, etc) that
 * follows the respect school link format (See UrlExt.schoolUrlOrNull) resolve this into a
 * NavCommand.
 */
class ResolveUrlToNavCommandUseCase(
    private val respectAccountManager: RespectAccountManager
) {

    operator fun invoke(
        url: Url,
        canGoBack: Boolean = true,
    ): NavCommand? {
        val schoolUrl = url.schoolUrlOrNull() ?: return null

        val lastSegment = url.segments.lastOrNull() ?: return null

        return when (lastSegment) {
            CreateInviteLinkUseCase.PATH -> {
                url.parameters[CreateInviteLinkUseCase.QUERY_PARAM]?.let { inviteCode ->
                    val destination = if (respectAccountManager.activeAccount != null) {

                        SelectAccount(
                            inviteCode = inviteCode
                        )
                    } else {

                        LoginScreen.create(
                            schoolUrl = schoolUrl,
                            inviteCode = inviteCode,
                        )
                    }

                    NavCommand.Navigate(
                        destination = destination,
                        clearBackStack = false
                    )
                }
            }
            else -> null
        }
    }

}