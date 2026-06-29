package world.respect.app.view.bookmark

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.langMapString
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.findIcons
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.bookmark
import world.respect.shared.generated.resources.msg_see_bookmark
import world.respect.shared.generated.resources.no_bookmark
import world.respect.shared.viewmodel.bookmark.BookmarkListUiState
import world.respect.shared.viewmodel.bookmark.BookmarkListViewModel

@Composable
fun BookmarkListScreen(
    viewModel: BookmarkListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    BookmarkListScreen(
        uiState = uiState,
        onClickRemoveBookmark = viewModel::onClickRemoveBookmark,
        onClickBookmark = viewModel::onClickBookmark
    )
}

@Composable
fun BookmarkListScreen(
    uiState: BookmarkListUiState,
    onClickRemoveBookmark: (XapiStatement) -> Unit,
    onClickBookmark: (XapiStatement) -> Unit
) {

    when {
        uiState.statements.isEmpty() -> {
            EmptyBookmarkState()
        }

        else -> {
            BookmarkListContent(
                uiState.statements,
                uiState.publications,
                onClickRemoveBookmark,
                onClickBookmark
            )
        }
    }
}


@Composable
private fun EmptyBookmarkState() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.no_bookmark),
                contentDescription = stringResource(resource = Res.string.no_bookmark),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(200.dp)
            )
            Text(
                text = stringResource(Res.string.no_bookmark),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(Res.string.msg_see_bookmark),
                modifier = Modifier.padding(bottom = 64.dp)
            )
        }
    }
}


@Composable
private fun BookmarkListContent(
    statements: List<XapiStatement>,
    publications: Map<String, OpdsPublication>,
    onClickRemoveBookmark: (XapiStatement) -> Unit,
    onClickBookmark: (XapiStatement) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(statements) { statement ->
            val activityId = (statement.`object` as? XapiActivity)?.id
            val publication = activityId?.let { publications[it] }

            ListItem(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        onClickBookmark(statement)
                    },

                leadingContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageUrl = publication?.findIcons()?.firstOrNull()?.href
                        if (imageUrl != null) {
                            RespectAsyncImage(
                                uri = imageUrl,
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                },

                headlineContent = {
                    Text(text = publication?.metadata?.title?.let { langMapString(it) }
                        ?: activityId ?: "")
                },

                supportingContent = {
                    Column {
                        Text(publication?.metadata?.subtitle?.let { langMapString(it) } ?: "")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            publication?.metadata?.language?.firstOrNull()?.let { langCode ->
                                val languageName = java.util.Locale(langCode).displayLanguage
                                Text(text = languageName)
                            }

                            publication?.metadata?.type?.toString()
                                ?.substringAfterLast("/")?.let { type ->
                                    Text(text = type)
                                }
                        }
                    }
                },

                trailingContent = {
                    Icon(
                        modifier = Modifier.clickable {
                            onClickRemoveBookmark(statement)
                        },
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(Res.string.bookmark),
                    )
                }
            )
        }
    }
}
