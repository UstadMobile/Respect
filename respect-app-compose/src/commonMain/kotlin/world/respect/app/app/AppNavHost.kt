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
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.app.view.learningunit.detail.LearningUnitDetailScreen
import world.respect.app.view.learningunit.list.LearningUnitListScreen
import world.respect.app.view.report.detail.ReportDetailScreen
import world.respect.app.view.report.edit.ReportEditScreen
import world.respect.app.view.report.list.ReportListScreen
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.assignments.AssignmentViewModel
import world.respect.shared.viewmodel.clazz.ClazzViewModel
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.app.viewmodel.respectViewModel
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RespectAppList
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.Assignment
import world.respect.shared.navigation.Clazz
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.navigation.Report
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.RespectComposeNavController
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel
import world.respect.shared.viewmodel.report.edit.ReportEditViewModel
import world.respect.shared.viewmodel.report.list.ReportListViewModel


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
        startDestination = RespectAppLauncher,
        modifier = modifier,
    ) {

        composable<RespectAppLauncher> {
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

        composable<ReportDetail> {
            val viewModel: ReportDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportDetailScreen(navController = navController, viewModel = viewModel)
        }
        composable<ReportEdit> {
            val viewModel: ReportEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportEditScreen(navController = navController, viewModel = viewModel)
        }
        composable<Report> {
            val viewModel: ReportListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportListScreen(navController = navController, viewModel = viewModel)
        }

        composable<RespectAppList> {
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
            EnterLinkScreen(viewModel = viewModel)
        }

        composable<LearningUnitList> {
            val viewModel: LearningUnitListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LearningUnitListScreen( viewModel = viewModel)
        }

        composable<LearningUnitDetail> {
            val viewModel: LearningUnitDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LearningUnitDetailScreen(viewModel = viewModel)
        }
    }

}



