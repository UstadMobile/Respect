package world.respect.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import world.respect.app.R


private val nameMap = mapOf(
    RespectImage.SPIX_LOGO to R.drawable.spix_logo,
    RespectImage.DIGITAL_LIBRARY to R.drawable.digital_library,
    RespectImage.WORKS_OFFLINE to R.drawable.works_offline,
    RespectImage.DATA_REPORTING to R.drawable.data_reporting,
    RespectImage.ASSIGNMENTS to R.drawable.assignments,
    RespectImage.ENTER_INVITE_CODE to R.drawable.enter_invite_code,
    RespectImage.OPEN_RESPECT_APP to R.drawable.open_respect_app,
    RespectImage.REVIEW_AND_COMPLETE_SETUP to R.drawable.review_and_complete_setup,
    RespectImage.SEARCH_SCHOOL to R.drawable.search_school,
    RespectImage.SHARE_INVITE_CODE to R.drawable.share_invite_code
)

@Composable
actual fun respectImagePainter(image: RespectImage): Painter {
    return painterResource(nameMap[image] ?: throw IllegalArgumentException("no image for $image"))

}