package world.respect.app.app

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlin.Boolean
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.apps
import world.respect.shared.generated.resources.assignments
import world.respect.shared.generated.resources.clazz
import world.respect.shared.generated.resources.people
import world.respect.shared.generated.resources.reports
import world.respect.shared.navigation.AccountList
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.Assignment
import world.respect.shared.navigation.Clazz
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.Report
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher

data class TopNavigationItem(
    val destRoute: Any,
    val icon: ImageVector,
    val label: StringResource
)

val APP_TOP_LEVEL_NAV_ITEMS = listOf(
    TopNavigationItem(
        destRoute = RespectAppLauncher,
        icon = Icons.Filled.GridView,
        label = Res.string.apps
    ),
    TopNavigationItem(
        destRoute = Assignment,
        icon = Icons.Filled.ImportContacts,
        label = Res.string.assignments
    ),
    TopNavigationItem(
        destRoute = Clazz,
        icon = Icons.AutoMirrored.Filled.LibraryBooks,
        label = Res.string.clazz
    ),
    TopNavigationItem(
        destRoute = Report,
        icon = Icons.Filled.BarChart,
        label = Res.string.reports
    ),
    TopNavigationItem(
        destRoute = PersonList,
        icon = Icons.Filled.Person,
        label = Res.string.people,
    ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App(
    widthClass: SizeClass = SizeClass.MEDIUM,
    useBottomBar: Boolean = true,
    onAppStateChanged: (AppUiState) -> Unit = { }) {
    val appUiState = remember {
        mutableStateOf(
            AppUiState(
                navigationVisible = true,
                hideAppBar = false,
            )
        )
    }

    val navController = rememberNavController()
    var appUiStateVal by appUiState
    LaunchedEffect(appUiStateVal) {
        onAppStateChanged(appUiStateVal)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val onShowSnackBar: SnackBarDispatcher = remember {
        SnackBarDispatcher { snack ->
            scope.launch {
                snackbarHostState.showSnackbar(snack.message, snack.action)
            }
        }
    }
    CompositionLocalProvider(LocalWidthClass provides widthClass) {
        Scaffold(
            topBar = {
                if (!appUiStateVal.hideAppBar) {
                    RespectAppBar(
                        compactHeader = (widthClass != SizeClass.EXPANDED),
                        appUiState = appUiStateVal,
                        navController = navController,
                        onProfileClick = {
                            navController.navigate(AccountList)
                        }
                    )
                }
            },
            bottomBar = {
                var selectedTopLevelItemIndex by remember { mutableIntStateOf(0) }
                if (useBottomBar) {
                    if (appUiStateVal.navigationVisible && !appUiStateVal.hideBottomNavigation) {
                        NavigationBar {
                            APP_TOP_LEVEL_NAV_ITEMS.forEachIndexed { index, item ->
                                val label = stringResource(item.label)
                                NavigationBarItem(
                                    icon = {
                                        Icon(item.icon, contentDescription = null)
                                    },
                                    label = { Text(label) },
                                    selected = selectedTopLevelItemIndex == index,
                                    onClick = {
                                        navController.navigate(item.destRoute)  {
                                            popUpTo(0) { inclusive = true }
                                        }
                                        selectedTopLevelItemIndex = index
                                    }
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (appUiStateVal.fabState.visible) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.testTag("floating_action_button"),
                        onClick = appUiStateVal.fabState.onClick,
                        text = {
                            Text(
                                modifier = Modifier.testTag("floating_action_button_text"),
                                text = appUiStateVal.fabState.text?.let {
                                    uiTextStringResource(it)
                                } ?: ""
                            )
                        },
                        icon = {
                            val imageVector = when (appUiStateVal.fabState.icon) {
                                FabUiState.FabIcon.ADD -> Icons.Default.Add
                                FabUiState.FabIcon.EDIT -> Icons.Default.Edit
                                else -> null
                            }
                            if (imageVector != null) {
                                Icon(
                                    imageVector = imageVector,
                                    contentDescription = null,
                                )
                            }
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                onSetAppUiState = {
                    appUiStateVal = it
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

}
