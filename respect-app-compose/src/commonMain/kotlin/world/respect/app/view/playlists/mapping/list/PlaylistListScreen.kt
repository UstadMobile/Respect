package world.respect.app.view.playlists.mapping.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.playlists.mapping.list.*

@Composable
fun PlaylistListScreenForViewModel(
    viewModel: PlaylistListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    PlaylistListScreen(
        uiState = uiState,
        onClickFilter = viewModel::onClickFilter,
        onClickPlaylist = viewModel::onClickPlaylist,
        onClickDismissFabMenu = viewModel::onClickDismissFabMenu,
        onClickAddNew = viewModel::onClickAddNew,
        onClickAddFromLink = viewModel::onClickAddFromLink,
    )
}

@Composable
fun PlaylistListScreen(
    uiState: PlaylistListUiState = PlaylistListUiState(),
    onClickFilter: (PlaylistFilter) -> Unit = {},
    onClickPlaylist: (OpdsPublication) -> Unit = {},
    onClickDismissFabMenu: () -> Unit = {},
    onClickAddNew: () -> Unit = {},
    onClickAddFromLink: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = uiState.activeFilter == PlaylistFilter.ALL,
                        onClick = { onClickFilter(PlaylistFilter.ALL) },
                        label = { Text(stringResource(Res.string.all)) },
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.activeFilter == PlaylistFilter.MY_PLAYLISTS,
                        onClick = { onClickFilter(PlaylistFilter.MY_PLAYLISTS) },
                        label = { Text(stringResource(Res.string.my_playlists)) },
                    )
                }
            }

            if (uiState.showPlaylists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.empty),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(Res.string.no_playlist_yet))
                    Text(stringResource(Res.string.no_playlist_yet_description))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(
                        items = uiState.showPlaylists,
                        key = { index, publication ->
                            publication.metadata.identifier?.toString()
                                ?: "${publication.metadata.title.getTitle()}_$index"
                        }
                    ) { _, publication ->
                        PlaylistListItem(
                            publication = publication,
                            onClickPublication = { onClickPlaylist(publication) },
                        )
                    }
                }
            }
        }

        if (uiState.isFabMenuExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClickDismissFabMenu() },
            ) {}
        }

        if (uiState.isFabMenuExpanded) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 88.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier
                        .testTag("add_new"),
                    onClick = onClickAddNew,
                    icon =
                        { Icon(Icons.Filled.Add, null) },
                    text
                    = { Text(stringResource(Res.string.add_new)) },
                )
                ExtendedFloatingActionButton(
                    modifier = Modifier
                        .testTag("add_from_a_link"),
                    onClick = onClickAddFromLink,
                    icon =
                        { Icon(Icons.Filled.Link, null) },
                    text =
                        { Text(stringResource(Res.string.add_from_a_link)) },
                )
            }
        }
    }
}

@Composable
private fun PlaylistListItem(
    publication: OpdsPublication,
    onClickPublication: () -> Unit,
) {
    val sectionCount = publication.metadata.numberOfPages ?: 0
    val itemCount = publication.metadata.duration?.toInt() ?: 0

    val ownerUsername = publication.links
        .firstOrNull { link ->
            link.rel?.contains(MakePlaylistOpdsFeedUseCase.REL_OWNER) == true
        }
        ?.href
        ?.trimEnd('/')
        ?.substringAfterLast('/')
        ?.takeIf { it.isNotBlank() }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickPublication() },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(publication.metadata.title.getTitle())
        },
        supportingContent = {
            Column {
                Text(
                    stringResource(
                        Res.string.sections_and_items,
                        sectionCount,
                        itemCount,
                    )
                )
                if (ownerUsername != null) {
                    Text(
                        text = stringResource(Res.string.created_by, ownerUsername),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
    )
}