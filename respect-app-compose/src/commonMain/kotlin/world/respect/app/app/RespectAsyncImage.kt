package world.respect.app.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun RespectAsyncImage(
    uri: String?,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier
) {
    AsyncImage(
        model = uri,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}
