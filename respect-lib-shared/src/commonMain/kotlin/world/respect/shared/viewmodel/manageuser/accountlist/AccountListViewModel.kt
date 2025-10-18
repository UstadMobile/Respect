package world.respect.shared.viewmodel.manageuser.accountlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.libutil.ext.replaceOrAppend
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.RespectAccountAndPerson
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.accounts
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isSameAccount
import world.respect.shared.viewmodel.RespectViewModel

/**
 * @property selectedAccount if not null, the currently selected account
 * @property accounts other accounts that are signed-in, available, and the user can switch to (
 *           (not including the selectedAccount)
 */
data class AccountListUiState(
    val selectedAccount: RespectAccountAndPerson? = null,
    val accounts: List<RespectAccountAndPerson> = emptyList(),
)

class AccountListViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle){

    private val _uiState = MutableStateFlow(AccountListUiState())

    val uiState = _uiState.asStateFlow()

    private var emittedNavToGetStartedCommand = false

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.accounts.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }

        viewModelScope.launch {
            respectAccountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                _uiState.update { prev ->
                    prev.copy(selectedAccount = accountAndPerson)
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
                            GetStartedScreen, clearBackStack = true
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
                            RespectAccountAndPerson(
                                account = it,
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
                                        RespectAccountAndPerson(
                                            account = account,
                                            person = person.dataOrNull() ?: Person(
                                                guid = account.userGuid,
                                                givenName = "",
                                                familyName = "",
                                                roles = emptyList(),
                                                gender = PersonGenderEnum.UNSPECIFIED,
                                            )
                                        )
                                    ) {
                                        it.account.isSameAccount(account)
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
        respectAccountManager.selectedAccount = account
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(RespectAppLauncher(), clearBackStack = true)
        )
    }

    fun onClickAddAccount() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(GetStartedScreen))
    }
    fun onClickProfile() {
        uiState.value.selectedAccount?.also {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    PersonDetail(
                        guid = it.account.userGuid
                    )
                )
            )
        }
    }


    fun onClickLogout() {
        uiState.value.selectedAccount?.also {
            viewModelScope.launch {
                respectAccountManager.endSession(it.account)
            }
        }
    }

}