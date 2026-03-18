package world.respect.app.view.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.view.apps.launcher.AppLauncherScreen
import world.respect.app.view.playlists.mapping.list.PlaylistListScreenForViewModel
import world.respect.app.viewmodel.respectViewModel
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.apps
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.playlists
import world.respect.shared.navigation.RespectComposeNavController
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.playlists.mapping.list.PlaylistListViewModel

enum class HomeScreenTabs(val label: StringResource) {
    APPS(Res.string.apps),

    //Temporary example
    //PLAYLISTS(Res.string.textbooks)
    PLAYLISTS(Res.string.playlists)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    respectNavController: RespectComposeNavController,
    onSetAppUiState: (AppUiState) -> Unit,
) {
    val pagerState = rememberPagerState { HomeScreenTabs.entries.size }
    val scope = rememberCoroutineScope()
    val selectedTab = HomeScreenTabs.entries[pagerState.currentPage]

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if(HomeScreenTabs.entries.size > 1) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
            ) {
                HomeScreenTabs.entries.forEach { tab ->
                    Tab(
                        selected = pagerState.currentPage == tab.ordinal,
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(tab.ordinal)
                            }
                        },
                        text = {
                            Text(stringResource(tab.label))
                        }
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState
        ) {
            when(selectedTab) {
                HomeScreenTabs.APPS -> {
                    val viewModel: AppLauncherViewModel = respectViewModel(
                        onSetAppUiState = onSetAppUiState,
                        navController = respectNavController,
                    )

                    AppLauncherScreen(
                        viewModel = viewModel
                    )
                }

                /* Temporary example.
                HomeScreenTabs.PLAYLISTS -> {
                    AssignmentListScreen(
                        viewModel = respectViewModel(
                            onSetAppUiState = onSetAppUiState,
                            navController = respectNavController,
                        )
                    )
                }*/
                HomeScreenTabs.PLAYLISTS -> {
                    val viewModel: PlaylistListViewModel = respectViewModel(
                        onSetAppUiState = onSetAppUiState,
                        navController = respectNavController,
                    )
                    PlaylistListScreenForViewModel(viewModel = viewModel)
                }
            }
        }
    }
}