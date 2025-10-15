package world.respect.app.view.apps.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.empty_list
import world.respect.shared.generated.resources.empty_list_description
import world.respect.shared.generated.resources.more_info
import world.respect.shared.generated.resources.remove
import world.respect.app.app.RespectAsyncImage
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.apps.launcher.AppLauncherUiState
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull

@Composable
fun AppLauncherScreen(
    viewModel: AppLauncherViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    AppLauncherScreen(
        uiState = uiState,
        onClickApp = { viewModel.onClickApp(it) },
        onClickRemove = { viewModel.onClickRemove(it) },
        onSnackBarShown = { viewModel.clearSnackBar() }
    )
}


@Composable
fun AppLauncherScreen(
    uiState: AppLauncherUiState,
    onClickApp: (DataLoadState<RespectAppManifest>) -> Unit,
    onClickRemove: (DataLoadState<RespectAppManifest>) -> Unit,
    onSnackBarShown: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    uiState.snackbarMessage?.let { message ->
        LaunchedEffect(message) {
            snackBarHostState.showSnackbar(message)
            onSnackBarShown()
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.appList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CrueltyFree,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                    Text(
                        text = stringResource(resource = Res.string.empty_list),
                    )
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                    Text(
                        text = stringResource(resource = Res.string.empty_list_description),
                        textAlign = TextAlign.Center
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
                    itemsIndexed(
                        items = uiState.appList,
                        key = { index, app ->
                            app.metaInfo.url?.toString() ?: index
                        }
                    ) { index, app ->
                        AppGridItem(
                            app = app,
                            onClickApp = {
                                onClickApp(app)
                            },
                            onClickRemove = {
                                onClickRemove(app)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppGridItem(
    app: DataLoadState<RespectAppManifest>,
    onClickApp: () -> Unit,
    onClickRemove: () -> Unit
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
