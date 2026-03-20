package world.respect.app.view.playlists.mapping.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumContributorObject
import world.respect.lib.opds.model.ReadiumContributorStringValue
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_from_a_link
import world.respect.shared.generated.resources.add_new
import world.respect.shared.generated.resources.all
import world.respect.shared.generated.resources.empty
import world.respect.shared.generated.resources.my_playlists
import world.respect.shared.generated.resources.no_playlist_yet
import world.respect.shared.generated.resources.no_playlist_yet_description
import world.respect.shared.generated.resources.sections_and_items
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.playlists.mapping.list.PlaylistFilter
import world.respect.shared.viewmodel.playlists.mapping.list.PlaylistListUiState
import world.respect.shared.viewmodel.playlists.mapping.list.PlaylistListViewModel

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
                        modifier = Modifier.testTag("filter_chip_all"),
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.activeFilter == PlaylistFilter.MY_PLAYLISTS,
                        onClick = { onClickFilter(PlaylistFilter.MY_PLAYLISTS) },
                        label = { Text(stringResource(Res.string.my_playlists)) },
                        modifier = Modifier.testTag("filter_chip_my_playlists"),
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
                        contentDescription = stringResource(Res.string.no_playlist_yet),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(200.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.no_playlist_yet),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(Res.string.no_playlist_yet_description),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(
                        items = uiState.showPlaylists,
                        key = { _, publication ->
                            publication.metadata.identifier?.toString()
                                ?: publication.metadata.title.toString()
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
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
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
                    onClick = onClickAddNew,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(Res.string.add_new),
                        )
                    },
                    text = {
                        Text(text = stringResource(Res.string.add_new))
                    },
                )

                ExtendedFloatingActionButton(
                    onClick = onClickAddFromLink,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = stringResource(Res.string.add_from_a_link),
                        )
                    },
                    text = {
                        Text(text = stringResource(Res.string.add_from_a_link))
                    },
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

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickPublication() }
            .testTag("playlist_item_${publication.metadata.identifier}"),
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        headlineContent = {
            Text(
                text = publication.metadata.title.getTitle(),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        Res.string.sections_and_items,
                        sectionCount,
                        itemCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}