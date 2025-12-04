package world.respect.app.view.playlists.enrollment.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.apps
import world.respect.shared.generated.resources.playlists
import world.respect.shared.generated.resources.all
import world.respect.shared.generated.resources.school_playlists
import world.respect.shared.generated.resources.my_playlists
import world.respect.shared.generated.resources.add_playlist
import world.respect.shared.generated.resources.add_new
import world.respect.shared.generated.resources.add_from_link
import world.respect.shared.generated.resources.all_sections_items
import world.respect.shared.generated.resources.created_by
import world.respect.shared.generated.resources.unknown_error
import world.respect.shared.viewmodel.playlists.list.MainTab
import world.respect.shared.viewmodel.playlists.list.PlaylistsViewModel
import world.respect.shared.viewmodel.playlists.list.PlaylistTab
import world.respect.shared.viewmodel.playlists.list.PlaylistUiModel
import world.respect.shared.viewmodel.playlists.list.PlaylistsUiState

@Composable
fun PlaylistsScreenForViewModel(
    viewModel: PlaylistsViewModel,
    onPlaylistClick: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
    onAddFromLink: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    PlaylistsScreen(
        uiState = uiState,
        onMainTabSelected = viewModel::onMainTabSelected,
        onTabSelected = viewModel::onTabSelected,
        onPlaylistClick = onPlaylistClick,
        onCreatePlaylist = onCreatePlaylist,
        onAddFromLink = onAddFromLink
    )
}

@Composable
fun PlaylistsScreen(
    uiState: PlaylistsUiState = PlaylistsUiState(),
    onMainTabSelected: (MainTab) -> Unit = {},
    onTabSelected: (PlaylistTab) -> Unit = {},
    onPlaylistClick: (String) -> Unit = {},
    onCreatePlaylist: () -> Unit = {},
    onAddFromLink: () -> Unit = {}
) {
    var showOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PlaylistsTopBar(
                selectedMainTab = uiState.selectedMainTab,
                selectedTab = uiState.selectedTab,
                onMainTabSelected = onMainTabSelected,
                onTabSelected = onTabSelected
            )
        },
        floatingActionButton = {
            FloatingActionButtonWithOptions(
                showOptions = showOptions,
                onToggleOptions = { showOptions = !showOptions },
                onCreatePlaylist = onCreatePlaylist,
                onAddFromLink = onAddFromLink
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: stringResource(Res.string.unknown_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    PlaylistsList(
                        playlists = uiState.playlists,
                        onPlaylistClick = onPlaylistClick,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistsTopBar(
    selectedMainTab: MainTab,
    selectedTab: PlaylistTab,
    onMainTabSelected: (MainTab) -> Unit,
    onTabSelected: (PlaylistTab) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = stringResource(Res.string.apps),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = if (selectedMainTab == MainTab.APPS) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable { onMainTabSelected(MainTab.APPS) }
            )
            Text(
                text = stringResource(Res.string.playlists),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = if (selectedMainTab == MainTab.PLAYLISTS) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable { onMainTabSelected(MainTab.PLAYLISTS) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == PlaylistTab.ALL,
                onClick = { onTabSelected(PlaylistTab.ALL) },
                label = { Text(stringResource(Res.string.all)) }
            )
            FilterChip(
                selected = selectedTab == PlaylistTab.SCHOOL,
                onClick = { onTabSelected(PlaylistTab.SCHOOL) },
                label = { Text(stringResource(Res.string.school_playlists)) }
            )
            FilterChip(
                selected = selectedTab == PlaylistTab.MY_PLAYLISTS,
                onClick = { onTabSelected(PlaylistTab.MY_PLAYLISTS) },
                label = { Text(stringResource(Res.string.my_playlists)) }
            )
        }
    }
}

@Composable
private fun PlaylistsList(
    playlists: List<PlaylistUiModel>,
    onPlaylistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(
            items = playlists,
            key = { it.id }
        ) { playlist ->
            PlaylistItem(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist.id) }
            )
        }
    }
}

@Composable
private fun PlaylistItem(
    playlist: PlaylistUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playlist.initials,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = playlist.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.all_sections_items, playlist.sectionCount, playlist.itemCount),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.created_by, playlist.createdBy),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FloatingActionButtonWithOptions(
    showOptions: Boolean,
    onToggleOptions: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onAddFromLink: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(bottom = 80.dp)
    ) {
        if (showOptions) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                AddOptionButton(
                    text = stringResource(Res.string.add_new),
                    onClick = {
                        onCreatePlaylist()
                        onToggleOptions()
                    }
                )

                AddOptionButton(
                    text = stringResource(Res.string.add_from_link),
                    onClick = {
                        onAddFromLink()
                        onToggleOptions()
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = onToggleOptions,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.add_playlist),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(Res.string.playlists),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AddOptionButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}