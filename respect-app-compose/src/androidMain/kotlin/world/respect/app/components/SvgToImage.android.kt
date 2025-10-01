package world.respect.app.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.compose.AsyncImage
import java.nio.ByteBuffer

@Composable
actual fun SvgToImage(svgString: String) {
    val context = LocalContext.current

    val sub = svgString.substringAfter(",")
    val bytes = sub.toByteArray()
    val model = ImageRequest.Builder(context)
        .data(ByteBuffer.wrap(bytes))
        .decoderFactory(SvgDecoder.Factory())
        .build()

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
    )
}