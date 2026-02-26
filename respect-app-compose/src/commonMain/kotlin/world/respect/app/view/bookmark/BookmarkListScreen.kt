package world.respect.app.view.bookmark

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.theme.black
import com.ustadmobile.libuicompose.theme.white
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.RespectQuickActionButton
import world.respect.lib.opds.model.Bookmark
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.bookmark
import world.respect.shared.generated.resources.msg_see_bookmark
import world.respect.shared.generated.resources.no_bookmark
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.bookmark.BookmarkListUiState
import world.respect.shared.viewmodel.bookmark.BookmarkListViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel.Companion.ICON

@Composable
fun BookmarkListScreen(
    viewModel: BookmarkListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    BookmarkListScreen(
        uiState = uiState,
    )
}

@Composable
fun BookmarkListScreen(
    uiState: BookmarkListUiState
) {

    when {
        uiState.isLoading -> {
            CircularProgressIndicator()
        }

        uiState.bookmarks.isEmpty() -> {
            EmptyBookmarkState()
        }

        else -> {
            BookmarkListContent(uiState.bookmarks)
        }
    }
}


@Composable
private fun EmptyBookmarkState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.no_bookmark),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.msg_see_bookmark),
            )
        }
    }
}
@Composable
private fun BookmarkListContent(
    bookmarks: List<Bookmark>
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        items(bookmarks) { bookmark ->

            val bookmarkIcon = if (bookmark.isBookmarked) {
                Icons.Filled.Bookmark
            } else {
                Icons.Outlined.BookmarkBorder
            }
            ListItem(
                modifier = Modifier.fillMaxWidth(),

                leadingContent = {


                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        bookmark.iconUrl.also { icon ->
                            RespectAsyncImage(
                                uri = icon,
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                            )
                        }
                    }
                },

                headlineContent = {
                    Text(text = bookmark.title?:"")
                },

                supportingContent = {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(white)
                                    .border(1.dp, black, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                bookmark.appIcon.also { icon ->
                                    RespectAsyncImage(
                                        uri = icon,
                                        contentDescription = "",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(80.dp)

                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = bookmark.appName
                            )
                        }

                        Text(
                            text = bookmark.subtitle?:""
                        )

                    }
                },

                trailingContent = {
                    Icon(
                        imageVector = bookmarkIcon,
                        contentDescription = stringResource(Res.string.bookmark),
                    )
                }

            )
        }
    }
}
