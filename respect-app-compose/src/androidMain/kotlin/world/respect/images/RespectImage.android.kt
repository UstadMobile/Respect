package world.respect.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import world.respect.app.R


private val nameMap = mapOf(
    RespectImage.SPIX_LOGO to R.drawable.spix_logo,
)
@Composable
actual fun respectImagePainter(image: RespectImage): Painter {
    return painterResource(nameMap[image]  ?: throw IllegalArgumentException("no image for $image"))

}