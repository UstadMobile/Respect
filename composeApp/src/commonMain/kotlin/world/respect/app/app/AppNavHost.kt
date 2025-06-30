package world.respect.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import world.respect.navigation.RespectComposeNavController


@Composable
fun AppNavHost(
    navController: NavHostController,
    onSetAppUiState: (AppUiState) -> Unit,
    modifier: Modifier,
) {
    val respectNavController = remember {
        RespectComposeNavController(navController)
    }

    NavHost(
        navController = navController,
        startDestination = AppLauncher,
        modifier = modifier
    ) {

        composable<AppLauncher> {
            val viewModel: AppLauncherViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController,
            )

            AppLauncherScreen(
                viewModel = viewModel
            )
        }

        composable<AppsDetail> {
            val viewModel: AppsDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AppsDetailScreen(
                viewModel = viewModel
            )
        }

        composable<Assignment> {
            val viewModel: AssignmentViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AssignmentScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable<Clazz> {
            val viewModel: ClazzViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzScreen(navController = navController, viewModel = viewModel)
        }

        composable<Report> {
            val viewModel: ReportViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportScreen(navController = navController, viewModel = viewModel)
        }

        composable<AppList> {
            val viewModel: AppListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AppListScreen(viewModel = viewModel)
        }

        composable<EnterLink> {
            val viewModel: EnterLinkViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterLinkScreen(navController = navController, viewModel = viewModel)
        }

        composable<LessonList> {
            val viewModel: LessonListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LessonListScreen( viewModel = viewModel)
        }

        composable<LessonDetail> {
            val viewModel: LessonDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LessonDetailScreen(

                viewModel = viewModel)
        }
    }

}



