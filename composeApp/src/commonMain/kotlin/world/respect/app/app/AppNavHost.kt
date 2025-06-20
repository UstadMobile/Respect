package world.respect.app.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import world.respect.app.view.apps.launcher.AppLauncherScreen
import world.respect.app.view.apps.list.AppListScreen
import world.respect.app.view.apps.detail.AppsDetailScreen
import world.respect.app.view.assignments.AssignmentScreen
import world.respect.app.view.clazz.ClazzScreen
import world.respect.app.view.apps.enterlink.EnterLinkScreen
import world.respect.app.view.report.ReportScreen
import world.respect.app.appstate.AppUiState
import world.respect.app.view.lessons.detail.LessonDetailScreen
import world.respect.app.view.lessons.list.LessonListScreen
import world.respect.app.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.app.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.app.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.app.viewmodel.apps.list.AppListViewModel
import world.respect.app.viewmodel.assignments.AssignmentViewModel
import world.respect.app.viewmodel.clazz.ClazzViewModel
import world.respect.app.viewmodel.lessons.detail.LessonDetailViewModel
import world.respect.app.viewmodel.lessons.list.LessonListViewModel
import world.respect.app.viewmodel.report.ReportViewModel
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
                modelClass = AppLauncherViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController,
            )
            AppLauncherScreen(
                viewModel = viewModel,
                onClickAction={
                    navController.navigate(AppsDetail(
                        manifestUrl = it,
                    ))
                }
            )
        }

        composable<AppsDetail> {
            val viewModel = respectViewModel(
                modelClass = AppsDetailViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            val args =it.toRoute<AppsDetail>()
            AppsDetailScreen(viewModel = viewModel,
                manifestUrl = args.manifestUrl,
                navController = navController)
        }

        composable<Assignment> {
            val viewModel = respectViewModel(
                modelClass = AssignmentViewModel::class,
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
                modelClass = ClazzViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            ClazzScreen(navController=navController,viewModel=viewModel) }

        composable<Report> {
            val viewModel = respectViewModel(
                modelClass = ReportViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            ReportScreen(navController=navController,viewModel=viewModel) }

        composable<AppList> {
            val viewModel = respectViewModel(
                modelClass = AppListViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            AppListScreen(navController = navController, viewModel = viewModel)
        }

        composable<EnterLink> {
            val viewModel = respectViewModel(
                modelClass = EnterLinkViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            EnterLinkScreen(navController = navController, viewModel = viewModel)
        }
        composable<LessonList> {
            val viewModel = respectViewModel(
                modelClass = LessonListViewModel::class,
                onSetAppUiState = onSetAppUiState,
                navController = navController
            )
            LessonListScreen(navController = navController, viewModel = viewModel)
        }


        composable<LessonDetail> {
            val viewModel=respectViewModel(
                modelClass = LessonDetailViewModel::class,
                onSetAppUiState=onSetAppUiState,
                navController = navController
            )
            LessonDetailScreen(navController = navController, viewModel = viewModel)

        }

    }

}



