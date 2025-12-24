package world.respect.app.view.apps.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.respectRememberPager
import world.respect.app.components.uiTextStringResource
import world.respect.app.view.curriculum.mapping.list.PlaylistListScreen
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.*
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.apps.launcher.AppLauncherUiState
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.curriculum.mapping.list.PlaylistListViewModel


@Composable
fun AppLauncherScreen(
    viewModel: AppLauncherViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    AppLauncherScreen(
        uiState = uiState,
        onClickApp = { viewModel.onClickApp(it) },
        onClickRemove = { viewModel.onClickRemove(it) },
        onTabSelected = viewModel::onTabSelected,
        playlistListViewModel = viewModel.playlistListViewModel,
    )
}

@Composable
fun AppLauncherScreen(
    uiState: AppLauncherUiState,
    onClickApp: (DataLoadState<RespectAppManifest>) -> Unit,
    onClickRemove: (DataLoadState<RespectAppManifest>) -> Unit,
    onTabSelected: (Int) -> Unit,
    playlistListViewModel: PlaylistListViewModel,
) {
    val mainTabs = listOf(
        stringResource(Res.string.apps),
        stringResource(Res.string.playlists)
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
            1 -> PlaylistListScreen(
                viewModel = playlistListViewModel
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