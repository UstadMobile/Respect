package world.respect.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

enum class RespectImage {
    SPIX_LOGO,
    DIGITAL_LIBRARY,
    WORKS_OFFLINE,
    DATA_REPORTING,
    ASSIGNMENTS,

    ENTER_INVITE_CODE,
    OPEN_RESPECT_APP,
    REVIEW_AND_COMPLETE_SETUP,
    SEARCH_SCHOOL,
    SHARE_INVITE_CODE
}
@Composable
expect fun respectImagePainter(image: RespectImage): Painter