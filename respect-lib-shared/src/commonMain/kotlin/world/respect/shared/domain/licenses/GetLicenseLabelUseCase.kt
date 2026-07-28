package world.respect.shared.domain.licenses

import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText

class GetLicenseLabelUseCase {
    operator fun invoke(link: ReadiumLink): UiText {
        // Dummy implementation for now
        val licenseName = link.title ?: link.href.split("/").lastOrNull() ?: ""
        return licenseName.asUiText()
    }
}
