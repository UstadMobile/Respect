package world.respect.app.view.curriculum.mapping.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add
import world.respect.shared.generated.resources.add_from_link
import world.respect.shared.generated.resources.add_new
import world.respect.shared.generated.resources.add_playlist
import world.respect.shared.generated.resources.all
import world.respect.shared.generated.resources.apps
import world.respect.shared.generated.resources.my_playlists
import world.respect.shared.generated.resources.no_playlists_yet
import world.respect.shared.generated.resources.playlists
import world.respect.shared.generated.resources.school_playlists
import world.respect.shared.viewmodel.curriculum.mapping.list.CurriculumMappingListUiState
import world.respect.shared.viewmodel.curriculum.mapping.list.CurriculumMappingListViewModel
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping

@Composable
fun CurriculumMappingListScreen(
    uiState: CurriculumMappingListUiState = CurriculumMappingListUiState(),
    onClickMapping: (CurriculumMapping) -> Unit = {},
    onClickMoreOptions: (CurriculumMapping) -> Unit = {},
    onClickAdd: () -> Unit = {},
    onClickAddFromLink: () -> Unit = {},
) {
    var selectedMainTabIndex by remember { mutableIntStateOf(1) }
    var selectedFilterChipIndex by remember { mutableIntStateOf(0) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    val mainTabs = listOf(
        stringResource(Res.string.apps),
        stringResource(Res.string.playlists)
    )
    val filterChips = listOf(
        stringResource(Res.string.all),
        stringResource(Res.string.school_playlists),
        stringResource(Res.string.my_playlists)
    )

    Scaffold(
        floatingActionButton = {
            if (selectedMainTabIndex == 1) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isFabMenuExpanded) {
                        FloatingActionButton(
                            onClick = {
                                onClickAdd()
                                isFabMenuExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(16.dp)
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
                                onClickAddFromLink()
                                isFabMenuExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Link,
                                    contentDescription = stringResource(Res.string.add_from_link),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(Res.string.add_from_link),
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedMainTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                mainTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedMainTabIndex == index,
                        onClick = { selectedMainTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterChips.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedFilterChipIndex == index,
                        onClick = { selectedFilterChipIndex = index },
                        label = { Text(label) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.mappings.isEmpty()) {
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
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.mappings,
                            key = { mapping -> mapping.uid }
                        ) { mapping ->
                            MappingListItem(
                                mapping = mapping,
                                onClickMapping = onClickMapping
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MappingListItem(
    mapping: CurriculumMapping,
    onClickMapping: (CurriculumMapping) -> Unit
) {
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
    }
}

@Composable
fun CurriculumMappingListScreenForViewModel(
    viewModel: CurriculumMappingListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CurriculumMappingListScreen(
        uiState = uiState,
        onClickMapping = viewModel::onClickMapping,
        onClickMoreOptions = viewModel::onClickMoreOptions,
        onClickAdd = viewModel::onClickMap,
        onClickAddFromLink = viewModel::onClickAddFromLink
    )
}