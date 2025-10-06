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
import world.respect.app.view.clazz.list.ClazzListScreen
import world.respect.app.view.clazz.edit.ClazzEditScreen
import world.respect.app.view.clazz.detail.ClazzDetailScreen
import world.respect.app.view.clazz.addperson.AddPersonToClazzScreen
import world.respect.app.view.learningunit.detail.LearningUnitDetailScreen
import world.respect.app.view.learningunit.list.LearningUnitListScreen
import world.respect.app.view.manageuser.accountlist.AccountListScreen
import world.respect.app.view.manageuser.signup.SignupScreen
import world.respect.app.view.manageuser.confirmation.ConfirmationScreen
import world.respect.app.view.manageuser.joinclazzwithcode.JoinClazzWithCodeScreen
import world.respect.app.view.manageuser.login.LoginScreen
import world.respect.app.view.manageuser.waitingforapproval.WaitingForApprovalScreen
import world.respect.app.view.manageuser.createaccount.CreateAccountScreen
import world.respect.app.view.manageuser.enterpasswordsignup.EnterPasswordSignupScreen
import world.respect.app.view.manageuser.getstarted.GetStartedScreen
import world.respect.app.view.manageuser.howpasskeywork.HowPasskeyWorksScreen
import world.respect.app.view.manageuser.otheroption.OtherOptionsScreen
import world.respect.app.view.manageuser.otheroptionsignup.OtherOptionsSignupScreen
import world.respect.app.view.manageuser.termsandcondition.TermsAndConditionScreen
import world.respect.app.view.onboarding.OnboardingScreen
import world.respect.app.view.person.detail.PersonDetailScreen
import world.respect.app.view.person.edit.PersonEditScreen
import world.respect.app.view.person.list.PersonListScreen
import world.respect.app.view.person.manageaccount.ManageAccountScreen
import world.respect.app.view.person.passkeyList.PasskeyListScreen
import world.respect.app.view.report.detail.ReportDetailScreen
import world.respect.app.view.report.edit.ReportEditScreen
import world.respect.app.view.report.filteredit.ReportFilterEditScreen
import world.respect.app.view.report.indicator.detail.IndicatorDetailScreen
import world.respect.app.view.report.indicator.edit.IndictorEditScreen
import world.respect.app.view.report.indicator.list.IndicatorListScreen
import world.respect.app.view.report.list.ReportListScreen
import world.respect.app.view.report.list.ReportTemplateListScreen
import world.respect.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.assignments.AssignmentViewModel
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.app.viewmodel.respectViewModel
import world.respect.shared.navigation.AccountList
import world.respect.shared.navigation.Acknowledgement
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.AddPersonToClazz
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.Assignment
import world.respect.shared.navigation.ClazzList
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.ConfirmationScreen
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.IndicatorDetail
import world.respect.shared.navigation.IndicatorList
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.navigation.JoinClazzWithCode
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.LearningUnitList
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.Report
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.navigation.ReportTemplateList
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RespectAppList
import world.respect.shared.navigation.RespectComposeNavController
import world.respect.shared.viewmodel.clazz.edit.ClazzEditViewModel
import world.respect.shared.viewmodel.clazz.list.ClazzListViewModel
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel
import world.respect.shared.viewmodel.clazz.addperson.AddPersonToClazzViewModel
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.Onboarding
import world.respect.shared.navigation.OtherOption
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.navigation.PasskeyList
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.manageuser.confirmation.ConfirmationViewModel
import world.respect.shared.viewmodel.manageuser.enterpasswordsignup.EnterPasswordSignupViewModel
import world.respect.shared.viewmodel.manageuser.getstarted.GetStartedViewModel
import world.respect.shared.viewmodel.manageuser.howpasskeywork.HowPasskeyWorksViewModel
import world.respect.shared.viewmodel.manageuser.joinclazzwithcode.JoinClazzWithCodeViewModel
import world.respect.shared.viewmodel.manageuser.login.LoginViewModel
import world.respect.shared.viewmodel.manageuser.otheroption.OtherOptionsViewModel
import world.respect.shared.viewmodel.manageuser.otheroptionsignup.OtherOptionsSignupViewModel
import world.respect.shared.viewmodel.manageuser.profile.SignupViewModel
import world.respect.shared.viewmodel.manageuser.termsandcondition.TermsAndConditionViewModel
import world.respect.shared.viewmodel.manageuser.waitingforapproval.WaitingForApprovalViewModel
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel
import world.respect.shared.viewmodel.report.edit.ReportEditViewModel
import world.respect.shared.viewmodel.report.filteredit.ReportFilterEditViewModel
import world.respect.shared.viewmodel.report.indictor.detail.IndicatorDetailViewModel
import world.respect.shared.viewmodel.report.indictor.edit.IndicatorEditViewModel
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListViewModel
import world.respect.shared.viewmodel.report.list.ReportListViewModel
import world.respect.shared.viewmodel.report.list.ReportTemplateListViewModel
import world.respect.shared.viewmodel.manageuser.signup.CreateAccountViewModel
import world.respect.shared.viewmodel.onboarding.OnboardingViewModel

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

        composable<Onboarding>{
            val viewModel: OnboardingViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )

            OnboardingScreen(viewModel)
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

        composable<ClazzList> {
            val viewModel: ClazzListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzListScreen(
                viewModel = viewModel
            )
        }

        composable<ClazzEdit> {
            val viewModel: ClazzEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzEditScreen(
                viewModel = viewModel
            )
        }

        composable<ClazzDetail> {
            val viewModel: ClazzDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzDetailScreen(
                viewModel = viewModel
            )
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
            ReportEditScreen(viewModel = viewModel)
        }
        composable<Report> {
            val viewModel: ReportListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportListScreen(viewModel = viewModel)
        }
        composable<ReportTemplateList> {
            val viewModel: ReportTemplateListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportTemplateListScreen(viewModel = viewModel)
        }
        composable<IndictorEdit> {
            val viewModel: IndicatorEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndictorEditScreen(viewModel = viewModel)
        }
        composable<ReportEditFilter> {
            val viewModel: ReportFilterEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportFilterEditScreen(navController = navController, viewModel = viewModel)
        }
        composable<IndicatorList> {
            val viewModel: IndicatorListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndicatorListScreen(viewModel = viewModel)
        }
        composable<IndicatorDetail> {
            val viewModel: IndicatorDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndicatorDetailScreen(navController = navController, viewModel = viewModel)
        }

        composable<HowPasskeyWorks> {
            val viewModel: HowPasskeyWorksViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            HowPasskeyWorksScreen(viewModel = viewModel)
        }
        composable<RespectAppList> {
            val viewModel: AppListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AppListScreen(
                viewModel = viewModel
            )
        }
        composable<EnterLink> {
            val viewModel: EnterLinkViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterLinkScreen(
                viewModel = viewModel
            )
        }

        composable<GetStartedScreen> {
            val viewModel: GetStartedViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            GetStartedScreen(viewModel = viewModel)
        }

        composable<OtherOption> {
            val viewModel: OtherOptionsViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            OtherOptionsScreen(viewModel = viewModel)
        }

        composable<LearningUnitList> {
            val viewModel: LearningUnitListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LearningUnitListScreen(
                viewModel = viewModel
            )
        }

        composable<OtherOptionsSignup> {
            val viewModel: OtherOptionsSignupViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            OtherOptionsSignupScreen(viewModel = viewModel)
        }

        composable<EnterPasswordSignup> {
            val viewModel: EnterPasswordSignupViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterPasswordSignupScreen(viewModel = viewModel)
        }

        composable<LearningUnitDetail> {
            val viewModel: LearningUnitDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LearningUnitDetailScreen(
                viewModel = viewModel
            )
        }

        composable<AddPersonToClazz> {
            val viewModel: AddPersonToClazzViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AddPersonToClazzScreen(
                viewModel = viewModel
            )
        }

        composable<SignupScreen> {
            val viewModel: SignupViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SignupScreen(
                viewModel = viewModel
            )
        }

        composable<ConfirmationScreen> {
            val viewModel: ConfirmationViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ConfirmationScreen(
                viewModel = viewModel
            )
        }

        composable<TermsAndCondition> {
            val viewModel: TermsAndConditionViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            TermsAndConditionScreen(
                viewModel = viewModel
            )
        }

        composable<CreateAccount> {
            val viewModel: CreateAccountViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            CreateAccountScreen(
                viewModel = viewModel
            )
        }

        composable<WaitingForApproval> {
            val viewModel: WaitingForApprovalViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            WaitingForApprovalScreen(
                viewModel = viewModel
            )
        }

        composable<AccountList> {
            AccountListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonList> {
            PersonListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonDetail> {
            PersonDetailScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<ManageAccount> {
            ManageAccountScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }
        composable<PasskeyList> {
            PasskeyListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonEdit> {
            PersonEditScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

    }
}



