package world.respect.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import world.respect.app.view.acknowledgement.AcknowledgementScreen
import world.respect.app.view.apps.detail.AppsDetailScreen
import world.respect.app.view.apps.enterlink.EnterLinkScreen
import world.respect.app.view.apps.launcher.AppLauncherScreen
import world.respect.app.view.apps.list.AppListScreen
import world.respect.app.view.assignments.AssignmentScreen
import world.respect.app.view.clazz.ClazzScreen
import world.respect.app.view.learningunit.detail.LearningUnitDetailScreen
import world.respect.app.view.learningunit.list.LearningUnitListScreen
import world.respect.app.view.manageuser.confirmation.ConfirmationScreen
import world.respect.app.view.manageuser.createaccount.CreateAccountScreen
import world.respect.app.view.manageuser.joinclazzwithcode.JoinClazzWithCodeScreen
import world.respect.app.view.manageuser.login.LoginScreen
import world.respect.app.view.manageuser.signup.SignupScreen
import world.respect.app.view.manageuser.termsandcondition.TermsAndConditionScreen
import world.respect.app.view.manageuser.waitingforapproval.WaitingForApprovalScreen
import world.respect.app.view.report.detail.ReportDetailScreen
import world.respect.app.view.report.edit.ReportEditScreen
import world.respect.app.view.report.filteredit.ReportFilterEditScreen
import world.respect.app.view.report.indictor.IndictorEditScreen
import world.respect.app.view.report.list.ReportListScreen
import world.respect.app.view.report.list.ReportTemplateListScreen
import world.respect.app.viewmodel.respectViewModel
import world.respect.shared.navigation.Acknowledgement
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.Assignment
import world.respect.shared.navigation.Clazz
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.JoinClazzWithCode
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.Report
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.navigation.ReportIndictorEdit
import world.respect.shared.navigation.ReportTemplateList
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RespectAppList
import world.respect.shared.navigation.RespectComposeNavController
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.assignments.AssignmentViewModel
import world.respect.shared.viewmodel.clazz.ClazzViewModel
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.viewmodel.manageuser.confirmation.ConfirmationViewModel
import world.respect.shared.viewmodel.manageuser.joinclazzwithcode.JoinClazzWithCodeViewModel
import world.respect.shared.viewmodel.manageuser.login.LoginViewModel
import world.respect.shared.viewmodel.manageuser.profile.SignupViewModel
import world.respect.shared.viewmodel.manageuser.signup.CreateAccountViewModel
import world.respect.shared.viewmodel.manageuser.termsandcondition.TermsAndConditionViewModel
import world.respect.shared.viewmodel.manageuser.waitingforapproval.WaitingForApprovalViewModel
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel
import world.respect.shared.viewmodel.report.edit.ReportEditViewModel
import world.respect.shared.viewmodel.report.filteredit.ReportFilterEditViewModel
import world.respect.shared.viewmodel.report.indictor.IndictorEditViewmodel
import world.respect.shared.viewmodel.report.list.ReportListViewModel
import world.respect.shared.viewmodel.report.list.ReportTemplateListViewModel


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
        startDestination = Acknowledgement,
        modifier = modifier,
    ) {
        composable<Acknowledgement> {
            val viewModel: AcknowledgementViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AcknowledgementScreen(viewModel)
        }
        composable<LoginScreen> {
            val viewModel: LoginViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LoginScreen(viewModel)
        }

        composable<JoinClazzWithCode> {
            val viewModel: JoinClazzWithCodeViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            JoinClazzWithCodeScreen(viewModel)
        }
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
        composable<ReportTemplateList> {
            val viewModel: ReportTemplateListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportTemplateListScreen(navController = navController, viewModel = viewModel)
        }
        composable<ReportIndictorEdit> {
            val viewModel: IndictorEditViewmodel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndictorEditScreen(navController = navController, viewModel = viewModel)
        }
        composable<ReportEditFilter> {
            val viewModel: ReportFilterEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportFilterEditScreen(navController = navController, viewModel = viewModel)
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


        composable<SignupScreen> {
            val viewModel: SignupViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SignupScreen(
                viewModel = viewModel)
        }
        composable<ConfirmationScreen> {
            val viewModel: ConfirmationViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ConfirmationScreen(

                viewModel = viewModel)
        }
        composable<TermsAndCondition> {
            val viewModel: TermsAndConditionViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            TermsAndConditionScreen(
                viewModel = viewModel)
        }
        composable<CreateAccount> {
            val viewModel: CreateAccountViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            CreateAccountScreen(
                viewModel = viewModel)
        }
        composable<WaitingForApproval> {
            val viewModel: WaitingForApprovalViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            WaitingForApprovalScreen(
                viewModel = viewModel)
        }
    }

}



