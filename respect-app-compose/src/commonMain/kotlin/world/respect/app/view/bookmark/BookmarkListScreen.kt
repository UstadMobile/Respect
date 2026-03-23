package world.respect.app.view.bookmark

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ListItem
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
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Bookmark
import world.respect.datalayer.school.model.BookmarkDetails
import world.respect.lib.opds.model.findIcons
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.bookmark
import world.respect.shared.generated.resources.msg_see_bookmark
import world.respect.shared.generated.resources.no_bookmark
import world.respect.shared.viewmodel.app.appstate.getTitle
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
    onClickRemoveBookmark: (Bookmark) -> Unit,
    onClickBookmark: (Bookmark) -> Unit
) {

    when {
        uiState.bookmarkDetails.isEmpty() -> {
            EmptyBookmarkState()
        }

        else -> {
            BookmarkListContent(
                uiState.bookmarkDetails,
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
    bookmarkDetails: List<BookmarkDetails>,
    onClickRemoveBookmark: (Bookmark) -> Unit,
    onClickBookmark: (Bookmark) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(bookmarkDetails) { bookmarkDetails ->
            ListItem(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        onClickBookmark(bookmarkDetails.bookmark)
                    },

                leadingContent = {

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RespectAsyncImage(
                            uri = bookmarkDetails.bookmark.imageUrl.toString(),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                        )
                    }

                },

                headlineContent = {
                    Text(
                        text = bookmarkDetails.bookmark.title?.getTitle() ?: ""
                    )
                },

                supportingContent = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bookmarkDetails.app?.dataOrNull()?.findIcons()?.firstOrNull()?.toString()
                                .also { icon ->
                                    RespectAsyncImage(
                                        uri = icon,
                                        contentDescription = "",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(20.dp)

                                    )
                                }


                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = bookmarkDetails.app?.dataOrNull()?.metadata?.title?.getTitle()
                                    .orEmpty()
                            )
                        }
                        /**Currently there is no data in subtitle**/
                       /* Text(
                            text = bookmark.subTitle?.getTitle() ?: ""
                        )*/

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bookmarkDetails.bookmark.language?.let {
                                Text(text = it)
                            }
                            /**Currently there is no data in grade**/
                           /* bookmark.grade?.let {
                                Text(text = it)
                            }*/

                            bookmarkDetails.bookmark.type?.let {
                                Text(text = it)
                            }
                        }

                    }
                },


                trailingContent = {
                    Icon(
                        modifier = Modifier.clickable {
                            onClickRemoveBookmark(bookmarkDetails.bookmark)
                        },
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(Res.string.bookmark),
                    )
                }
            )
        }
    }
}
