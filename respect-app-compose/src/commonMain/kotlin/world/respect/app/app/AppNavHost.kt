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
import world.respect.app.view.assignments.detail.AssignmentDetailScreen
import world.respect.app.view.assignments.edit.AssignmentEditScreen
import world.respect.app.view.assignments.list.AssignmentListScreen
import world.respect.app.view.clazz.ClazzScreen
import world.respect.app.view.learningunit.detail.LearningUnitDetailScreen
import world.respect.app.view.learningunit.list.LearningUnitListScreen
import world.respect.app.view.manageuser.accountlist.RespectAccountListScreen
import world.respect.app.view.manageuser.signup.SignupScreen
import world.respect.app.view.manageuser.confirmation.ConfirmationScreen
import world.respect.app.view.manageuser.joinclazzwithcode.JoinClazzWithCodeScreen
import world.respect.app.view.manageuser.login.LoginScreen
import world.respect.app.view.manageuser.waitingforapproval.WaitingForApprovalScreen
import world.respect.app.view.manageuser.createaccount.CreateAccountScreen
import world.respect.app.view.manageuser.termsandcondition.TermsAndConditionScreen
import world.respect.app.view.report.ReportScreen
import world.respect.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import world.respect.app.viewmodel.respectViewModel
import world.respect.shared.navigation.Acknowledgement
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.Clazz
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.JoinClazzWithCode
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.Report
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RespectAppList
import world.respect.shared.navigation.RespectComposeNavController
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.RespectAccountList
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.assignments.list.AssignmentViewModel
import world.respect.shared.viewmodel.clazz.ClazzListViewModel
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.viewmodel.manageuser.confirmation.ConfirmationViewModel
import world.respect.shared.viewmodel.manageuser.joinclazzwithcode.JoinClazzWithCodeViewModel
import world.respect.shared.viewmodel.manageuser.login.LoginViewModel
import world.respect.shared.viewmodel.manageuser.profile.SignupViewModel
import world.respect.shared.viewmodel.manageuser.termsandcondition.TermsAndConditionViewModel
import world.respect.shared.viewmodel.manageuser.waitingforapproval.WaitingForApprovalViewModel
import world.respect.shared.viewmodel.report.ReportViewModel
import world.respect.shared.viewmodel.manageuser.signup.CreateAccountViewModel


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

        composable<AssignmentList> {
            val viewModel: AssignmentViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AssignmentListScreen(
                viewModel = viewModel
            )
        }

        composable<AssignmentEdit> {
            AssignmentEditScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<AssignmentDetail> {
            AssignmentDetailScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<Clazz> {
            val viewModel: ClazzListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzScreen(viewModel = viewModel)
        }

        composable<Report> {
            val viewModel: ReportViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportScreen(navController = navController, viewModel = viewModel)
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

        composable<RespectAccountList> {
            RespectAccountListScreen(
                respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
    }

}



