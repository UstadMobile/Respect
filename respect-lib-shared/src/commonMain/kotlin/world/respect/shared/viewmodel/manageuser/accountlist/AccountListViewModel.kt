package world.respect.shared.viewmodel.manageuser.accountlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ext.getClassName
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.school.model.PersonWithEnrollment
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.libutil.ext.replaceOrAppend
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSession
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.accounts
import world.respect.shared.generated.resources.select_account
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.AccountList
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.EnterInviteCode
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.Home
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isSameAccount
import world.respect.shared.viewmodel.RespectViewModel

/**
 * @property selectedAccount if not null, the currently selected account
 * @property accounts other accounts that are signed-in, available, and the user can switch to (
 *           (not including the selectedAccount)
 */
data class AccountListUiState(
    val selectedAccount: RespectSessionAndPerson? = null,
    val accounts: List<RespectSessionAndPerson> = emptyList(),
    val isPendingExpanded: Boolean = true,
    val isSelectAccountMode : Boolean = false,
    val pendingEnrolmentPerson: List<PersonWithEnrollment> = emptyList()
) {
    val showSelectedAccountProfileButton: Boolean
        get() = selectedAccount?.person?.status != PersonStatusEnum.PENDING_APPROVAL

    val familyMembersClickEnabled: Boolean
        get() = selectedAccount?.person?.status != PersonStatusEnum.PENDING_APPROVAL

}

class AccountListViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val route: AccountList = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(AccountListUiState(isSelectAccountMode = route.inviteCode!=null))

    private val schoolDataSource: SchoolDataSource by inject()


    val uiState = _uiState.asStateFlow()


    private var emittedNavToGetStartedCommand = false

    init {
        _appUiState.update {
            it.copy(
                title = if (_uiState.value.isSelectAccountMode) Res.string.select_account.asUiText() else
                    Res.string.accounts.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }

        viewModelScope.launch {
            val parentGuid = respectAccountManager.activeAccount?.userGuid ?: return@launch

            val children = schoolDataSource.personDataSource.list(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    common = GetListCommonParams(guid = parentGuid),
                    includeRelated = true
                )
            ).dataOrNull()
                ?.filter { it.guid != parentGuid }
                .orEmpty()

            val childMap = children.associateBy { it.guid }

            val enrollments = schoolDataSource.enrollmentDataSource.list(
                loadParams = DataLoadParams(),
                listParams = EnrollmentDataSource.GetListParams(
                    role = EnrollmentRoleEnum.PENDING_STUDENT
                )
            ).dataOrNull().orEmpty()

            val pending = enrollments.mapNotNull { e ->
                val person = childMap[e.personUid] ?: return@mapNotNull null


                PersonWithEnrollment(person, e.getClassName()?:"", e)
            }

            respectAccountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                _uiState.update {
                    it.copy(
                        selectedAccount = accountAndPerson,
                        pendingEnrolmentPerson = pending
                    )
                }
            }
        }

        viewModelScope.launch {
            respectAccountManager.accounts.combine(
                respectAccountManager.selectedAccountFlow
            ) { storedAccounts, activeAccount ->
                Pair(storedAccounts, activeAccount)
            }.collectLatest { (storedAccounts, activeAccount) ->
                /*
                 * If there are no stored accounts (eg because they have logged out of all accounts),
                 * or if a session is terminated remotely (eg password reset), then must go to
                 * GetStarted screen.
                 */
                if(storedAccounts.isEmpty() && !emittedNavToGetStartedCommand) {
                    emittedNavToGetStartedCommand = true
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            GetStartedScreen(), clearBackStack = true
                        )
                    )

                    return@collectLatest
                }

                //As noted on UiState - the active account is removed from the list of other
                //accounts
                val storedAccountList = storedAccounts.filterNot {
                    activeAccount?.isSameAccount(it) == true
                }

                _uiState.update { prev ->
                    prev.copy(
                        accounts = storedAccountList.map {
                            RespectSessionAndPerson(
                                session = RespectSession(it, null),
                                person = Person(
                                    guid = it.userGuid,
                                    givenName = "",
                                    familyName = "",
                                    roles = emptyList(),
                                    gender = PersonGenderEnum.UNSPECIFIED,
                                )
                            )
                        }
                    )
                }

                storedAccountList.forEach { account ->
                    launch {
                        val accountScope = respectAccountManager.getOrCreateAccountScope(account)
                        val dataSource: SchoolDataSource = accountScope.get()
                        dataSource.personDataSource.findByGuidAsFlow(
                            account.userGuid
                        ).collect { person ->
                            _uiState.update { prev ->
                                prev.copy(
                                    accounts = prev.accounts.replaceOrAppend(
                                        RespectSessionAndPerson(
                                            session = RespectSession(account, null),
                                            person = person.dataOrNull() ?: Person(
                                                guid = account.userGuid,
                                                givenName = "",
                                                familyName = "",
                                                roles = emptyList(),
                                                gender = PersonGenderEnum.UNSPECIFIED,
                                            )
                                        )
                                    ) {
                                        it.session.account.isSameAccount(account)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onClickAccount(account: RespectAccount) {
        respectAccountManager.switchAccount(account)

        viewModelScope.launch {
            val accountScope = respectAccountManager.getOrCreateAccountScope(account)
            val person = accountScope.get<SchoolDataSource>().personDataSource.findByGuid(
                loadParams = DataLoadParams(onlyIfCached = true),
                guid = account.userGuid
            )

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = if(person.dataOrNull()?.status != PersonStatusEnum.PENDING_APPROVAL) {
                        if (uiState.value.isSelectAccountMode)
                        {
                            AcceptInvite.create(
                                schoolUrl = account.school.self,
                                code = route.inviteCode?:"",
                                canGoBack = true,
                            )
                        }else{
                            Home
                        }
                    }else {
                        WaitingForApproval()
                    },
                    clearBackStack = true
                )
            )
        }


    }
    fun onTogglePendingSection() {
        _uiState.update { it.copy(isPendingExpanded = !it.isPendingExpanded) }
    }
    fun onClickFamilyPerson(person: Person) {
        viewModelScope.launch {
            respectAccountManager.switchProfile(person.guid)
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = if(person.roles.firstOrNull()?.roleEnum == PersonRoleEnum.PARENT) {
                        Home
                    } else {
                        AssignmentList
                    },
                    clearBackStack = true
                )
            )
        }
    }

    fun onClickAddAccount() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                if (uiState.value.isSelectAccountMode){
                    LoginScreen(
                        schoolUrlStr = respectAccountManager.activeAccount?.school?.self.toString(),
                        inviteCode = route.inviteCode
                    )
                }else{
                    GetStartedScreen(canGoBack = true)
                }
            )
        )
    }

    fun onClickProfile() {
        uiState.value.selectedAccount?.also {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    PersonDetail(
                        guid = it.session.account.userGuid
                    )
                )
            )
        }
    }
    fun onClickEnterInviteCode() {
        uiState.value.selectedAccount?.also {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    EnterInviteCode(
                        schoolUrlStr = it.session.account.school.self.toString(),
                        personGuid = it.session.account.userGuid
                    )
                )
            )
        }
    }


    fun onClickLogout() {
        uiState.value.selectedAccount?.also {
            viewModelScope.launch {
                respectAccountManager.removeAccount(it.session.account)
            }
        }
    }

}