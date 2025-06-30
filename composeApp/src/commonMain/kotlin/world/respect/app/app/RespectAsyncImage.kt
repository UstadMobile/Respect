package world.respect.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.eygraber.uri.Uri

@Composable
fun RespectAsyncImage(
    uri: String,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier
) {
    AsyncImage(
        model = remember { Uri.parse(uri) },
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}
