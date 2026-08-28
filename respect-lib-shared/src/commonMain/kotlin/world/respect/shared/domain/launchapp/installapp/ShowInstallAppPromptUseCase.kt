package world.respect.shared.domain.launchapp.installapp

import world.respect.lib.opds.model.OpdsPublication

interface ShowInstallAppPromptUseCase {

    data class Request(
        val launchableApp: OpdsPublication,
    )

    suspend operator fun invoke(
        request: Request,
    )

}