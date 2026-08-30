package world.respect.shared.domain.launchapp.gotoappstore

import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink

interface GoToAppStoreUseCase {

    data class Request(
        val launchableApp: OpdsPublication,
        val referrer: String,
        val preferredStoreLink: ReadiumLink? = null,
    )

    suspend operator fun invoke(
        request: Request,
    )

}