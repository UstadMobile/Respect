package world.respect.app.view.playlists.mapping.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.*
import world.respect.shared.viewmodel.playlists.mapping.list.PlaylistListUiState
import world.respect.shared.viewmodel.playlists.mapping.list.PlaylistListViewModel

import world.respect.shared.viewmodel.playlists.mapping.model.PlaylistsMapping

@Composable
fun PlaylistListScreen(
    viewModel: PlaylistListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    PlaylistListScreenContent(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onClickMapping = viewModel::onClickMapping,
        onClickAddNew = viewModel::onClickAddNew,
        onClickAddLink = viewModel::onClickAddLink,
        onRemoveMapping = viewModel::removeMapping,
    )
}

@Composable
fun PlaylistListScreenContent(
    uiState: PlaylistListUiState,
    onFilterSelected: (Int) -> Unit,
    onClickMapping: (PlaylistsMapping) -> Unit,
    onClickAddNew: () -> Unit,
    onClickAddLink: () -> Unit,
    onRemoveMapping: (PlaylistsMapping) -> Unit,
) {
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    val filterChips = listOf(
        stringResource(Res.string.all),
        stringResource(Res.string.school_playlists),
        stringResource(Res.string.my_playlists)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterChips.forEachIndexed { index, label ->
                    FilterChip(
                        selected = uiState.selectedFilterIndex == index,
                        onClick = { onFilterSelected(index) },
                        label = { Text(label) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.filteredMappings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.no_playlists_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 88.dp
                        )
                    ) {
                        items(
                            items = uiState.filteredMappings,
                            key = { mapping -> mapping.uid }
                        ) { mapping ->
                            MappingListItem(
                                mapping = mapping,
                                onClickMapping = onClickMapping,
                                onRemoveMapping = onRemoveMapping,
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isFabMenuExpanded) {
                FloatingActionButton(
                    onClick = {
                        onClickAddNew()
                        isFabMenuExpanded = false
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(Res.string.add_new),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(Res.string.add_new),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        onClickAddLink()
                        isFabMenuExpanded = false
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Link,
                            contentDescription = stringResource(Res.string.add_from_a_link),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(Res.string.add_from_a_link),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { isFabMenuExpanded = !isFabMenuExpanded },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(Res.string.add),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(Res.string.add_playlist),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MappingListItem(
    mapping: PlaylistsMapping,
    onClickMapping: (PlaylistsMapping) -> Unit,
    onRemoveMapping: (PlaylistsMapping) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickMapping(mapping) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Filled.Book,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = mapping.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Filled.Book,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${mapping.sections.size} section, ${mapping.sections.sumOf { it.items.size }} items",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "",
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(Res.string.remove))
                    },
                    onClick = {
                        menuExpanded = false
                        onRemoveMapping(mapping)
                    }
                )
            }
        }
    }
}