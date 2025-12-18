package world.respect.app.view.apps.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.respectRememberPager
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add
import world.respect.shared.generated.resources.add_from_a_link
import world.respect.shared.generated.resources.add_from_link
import world.respect.shared.generated.resources.add_new
import world.respect.shared.generated.resources.add_playlist
import world.respect.shared.generated.resources.all
import world.respect.shared.generated.resources.apps
import world.respect.shared.generated.resources.empty
import world.respect.shared.generated.resources.empty_list
import world.respect.shared.generated.resources.more_info
import world.respect.shared.generated.resources.my_playlists
import world.respect.shared.generated.resources.no_playlists_yet
import world.respect.shared.generated.resources.playlists
import world.respect.shared.generated.resources.remove
import world.respect.shared.generated.resources.school_playlists
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.apps.launcher.AppLauncherUiState
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping

@Composable
fun AppLauncherScreen(
    viewModel: AppLauncherViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    AppLauncherScreen(
        uiState = uiState,
        onClickApp = { viewModel.onClickApp(it) },
        onClickRemove = { viewModel.onClickRemove(it) },
        onClickMapping = viewModel::onClickMapping,
        onClickMoreOptions = viewModel::onClickMoreOptions,
        onTabSelected = viewModel::onTabSelected,
        onClickMap = viewModel::onClickMap,
        onClickAddLink = viewModel::onClickAddLink,
        onRemoveMapping = viewModel::removeMapping,
    )
}

@Composable
fun AppLauncherScreen(
    uiState: AppLauncherUiState,
    onClickApp: (DataLoadState<RespectAppManifest>) -> Unit,
    onClickRemove: (DataLoadState<RespectAppManifest>) -> Unit,
    onClickMapping: (CurriculumMapping) -> Unit,
    onClickMoreOptions: (CurriculumMapping) -> Unit,
    onTabSelected: (Int) -> Unit,
    onClickMap: () -> Unit,
    onClickAddLink: () -> Unit,
    onRemoveMapping: (CurriculumMapping) -> Unit,
) {
    var selectedFilterChipIndex by remember { mutableIntStateOf(0) }

    val mainTabs = listOf(
        stringResource(Res.string.apps),
        stringResource(Res.string.playlists)
    )

    val filterChips = listOf(
        stringResource(Res.string.all),
        stringResource(Res.string.school_playlists),
        stringResource(Res.string.my_playlists)
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = uiState.selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            mainTabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }

        when (uiState.selectedTabIndex) {
            0 -> AppsTabContent(
                uiState = uiState,
                onClickApp = onClickApp,
                onClickRemove = onClickRemove,
            )
            1 -> PlaylistsTabContent(
                uiState = uiState,
                filterChips = filterChips,
                selectedFilterChipIndex = selectedFilterChipIndex,
                onFilterChipSelected = { selectedFilterChipIndex = it },
                onClickMapping = onClickMapping,
                onClickAdd = onClickMap,
                onClickAddLink = onClickAddLink,
                onRemoveMapping = onRemoveMapping,
            )
        }
    }
}

@Composable
private fun AppsTabContent(
    uiState: AppLauncherUiState,
    onClickApp: (DataLoadState<RespectAppManifest>) -> Unit,
    onClickRemove: (DataLoadState<RespectAppManifest>) -> Unit,
) {
    val pager = respectRememberPager(uiState.apps)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (lazyPagingItems.itemCount == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(Res.drawable.empty),
                    contentDescription = stringResource(resource = Res.string.empty_list),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                Text(
                    text = uiState.emptyListDescription?.let {
                        uiTextStringResource(it)
                    } ?: "",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = { index ->
                        lazyPagingItems.peek(index)?.uid ?: index.toString()
                    }
                ) { index ->
                    val schoolApp = lazyPagingItems[index]
                    val respectAppFlow = remember(schoolApp, uiState.respectAppForSchoolApp) {
                        schoolApp?.let { uiState.respectAppForSchoolApp(schoolApp) } ?: emptyFlow()
                    }
                    val respectApp by respectAppFlow.collectAsState(NoDataLoadedState.notFound())

                    AppGridItem(
                        app = respectApp,
                        onClickApp = {
                            onClickApp(respectApp)
                        },
                        onClickRemove = {
                            onClickRemove(respectApp)
                        },
                        showRemove = uiState.canRemove,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    uiState: AppLauncherUiState,
    filterChips: List<String>,
    selectedFilterChipIndex: Int,
    onFilterChipSelected: (Int) -> Unit,
    onClickMapping: (CurriculumMapping) -> Unit,
    onClickAdd: () -> Unit,
    onClickAddLink: () -> Unit,
    onRemoveMapping: (CurriculumMapping) -> Unit,
) {
    var isFabMenuExpanded by remember { mutableStateOf(false) }

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
                        selected = selectedFilterChipIndex == index,
                        onClick = { onFilterChipSelected(index) },
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
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            top = 8.dp,
                            bottom = 88.dp
                        )
                    ) {
                        items(
                            items = uiState.mappings,
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
                        onClickAdd()
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
    mapping: CurriculumMapping,
    onClickMapping: (CurriculumMapping) -> Unit,
    onRemoveMapping: (CurriculumMapping) -> Unit,
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

@Composable
fun AppGridItem(
    app: DataLoadState<RespectAppManifest>,
    onClickApp: () -> Unit,
    onClickRemove: () -> Unit,
    showRemove: Boolean = false,
) {
    val appData = app.dataOrNull()

    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onClickApp() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            appData?.icon.also { icon ->
                RespectAsyncImage(
                    uri = icon.toString(),
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
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
                            Text(stringResource(resource = Res.string.more_info))
                        },
                        onClick = {
                            menuExpanded = false
                            onClickApp()
                        }
                    )
                    if(showRemove) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(resource = Res.string.remove))
                            },
                            onClick = {
                                menuExpanded = false
                                onClickRemove()
                            }
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = appData?.name?.getTitle() ?: "",
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "-")
            Text(text = "-")
        }
    }
}