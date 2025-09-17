package world.respect.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import world.respect.app.R


private val nameMap = mapOf(
    RespectImage.SPIX_LOGO to R.drawable.spix_logo,
    RespectImage.DIGITAL_LIBRARY to R.drawable.digital_library_1,
    RespectImage.WORKS_OFFLINE to R.drawable.works_offline_1,
    RespectImage.DATA_REPORTING to R.drawable.data_reporting_1,
    RespectImage.ASSIGNMENTS to R.drawable.assignments_1
    )

@Composable
actual fun respectImagePainter(image: RespectImage): Painter {
    return painterResource(nameMap[image] ?: throw IllegalArgumentException("no image for $image"))

}