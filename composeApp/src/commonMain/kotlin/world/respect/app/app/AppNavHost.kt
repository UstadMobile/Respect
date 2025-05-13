package world.respect.app.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import world.respect.app.view.applauncher.AppLauncherScreen
import world.respect.app.view.applist.AppListScreen
import world.respect.app.view.appsdetail.AppsDetailScreen
import world.respect.app.view.assignments.AssignmentScreen
import world.respect.app.view.clazz.ClazzScreen
import world.respect.app.view.enterlink.EnterLinkScreen
import world.respect.app.view.report.ReportScreen
import world.respect.app.appstate.AppUiState
import world.respect.app.viewmodel.applauncher.AppLauncherScreenViewModel
import world.respect.app.viewmodel.applist.AppListScreenViewModel
import world.respect.app.viewmodel.appsdetail.AppsDetailScreenViewModel
import world.respect.app.viewmodel.assignments.AssignmentScreenViewModel
import world.respect.app.viewmodel.clazz.ClazzScreenViewModel
import world.respect.app.viewmodel.enterlink.EnterLnkScreenViewModel
import world.respect.app.viewmodel.report.ReportScreenViewModel
import world.respect.app.viewmodel.respectViewModel


@Composable
fun AppNavHost(
    navController: NavHostController,
    onSetAppUiState: (AppUiState) -> Unit,
    modifier: Modifier,
) {

    NavHost(navController = navController, startDestination = AppLauncher, modifier = modifier) {
        composable<AppLauncher> {

            val viewModel = respectViewModel(
                modelClass = AppLauncherScreenViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController,
            )
            AppLauncherScreen(
                navController = navController,
                viewModel = viewModel,
                onClickAction={
                    navController.navigate(AppsDetail(param="App id 1"))
                }
            )
        }
        composable<AppsDetail> {
            val viewModel = respectViewModel(
                modelClass = AppsDetailScreenViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            val args =it.toRoute<AppsDetail>()
            AppsDetailScreen(viewModel = viewModel,
                param = args.param)
        }

        composable<Assignment> {
            val viewModel = respectViewModel(
                modelClass = AssignmentScreenViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            AssignmentScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable<Clazz> {
            val viewModel = respectViewModel(
                modelClass = ClazzScreenViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            ClazzScreen(navController=navController,viewModel=viewModel) }
        composable<Report> {
            val viewModel = respectViewModel(
                modelClass = ReportScreenViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            ReportScreen(navController=navController,viewModel=viewModel) }
        composable<AppList> {
            val viewModel = respectViewModel(
                modelClass = AppListScreenViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            AppListScreen(navController = navController, viewModel = viewModel)
        }
        composable<EnterLink> {
            val viewModel = respectViewModel(
                modelClass = EnterLnkScreenViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            EnterLinkScreen(navController = navController, viewModel = viewModel)
        }
        composable<AppsDetail> {
            val viewModel = respectViewModel(
                modelClass = AppsDetailScreenViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            val args =it.toRoute<AppsDetail>()
            AppsDetailScreen(viewModel = viewModel,
                param = args.param)
        }

    }

}



