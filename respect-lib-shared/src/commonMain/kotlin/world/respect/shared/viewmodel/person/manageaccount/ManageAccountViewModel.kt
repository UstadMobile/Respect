package world.respect.shared.viewmodel.person.manageaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.personPassword.GetPersonPassword
import world.respect.datalayer.db.school.entities.PersonPasswordEntity
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountAndPerson
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.addpasskeyusecase.SavePersonPasskeyUseCase
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.toUserFriendlyString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.manage_account
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PasskeyList
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class ManageAccountUiState(
    val passkeyCount: Int? = null,
    val showCreatePasskey: Boolean = false,
    val passkeySupported: Boolean = true,
    val personUsername: String = "",
    val personPasswordEntity: PersonPasswordEntity? = null,
    val errorText: UiText? = null,
    val selectedAccount: RespectAccountAndPerson? = null,
)

class ManageAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val createPasskeyUseCase: CreatePasskeyUseCase?,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val savePersonPasskeyUseCase: SavePersonPasskeyUseCase by inject()

    private val getPersonPassword: GetPersonPassword by inject()

    private val route: ManageAccount = savedStateHandle.toRoute()

    private val personGuid = route.guid

    private val _uiState = MutableStateFlow(
        ManageAccountUiState()
    )

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update { prev ->
            prev.copy(
                userAccountIconVisible = false,
                navigationVisible = false,
                title = Res.string.manage_account.asUiText()
            )
        }

        viewModelScope.launch {
            val personPasswordEntity = getPersonPassword.getPersonPassword(personGuid)
            _uiState.update { prev ->
                prev.copy(
                    personPasswordEntity = personPasswordEntity
                )
            }
        }

        viewModelScope.launch {
            launch {
                schoolDataSource.personPasskeyDataSource.listAllAsFlow().collect {
                    _uiState.update { prev ->
                        prev.copy(passkeyCount = it.dataOrNull()?.size ?: 0)
                    }
                }
            }

            launch {
                schoolDataSource.personDataSource.findByGuidAsFlow(
                    route.guid
                ).collect {
                    _uiState.update { prev ->
                        prev.copy(personUsername = it.dataOrNull()?.username ?: "")
                    }
                }
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                _uiState.update { prev ->
                    prev.copy(selectedAccount = accountAndPerson)
                }
            }
        }

        _uiState.update { prev ->
            prev.copy(
                passkeySupported = (createPasskeyUseCase != null &&
                        accountManager.selectedAccount?.userGuid == personGuid),
            )
        }
    }

    fun navigateToEditAccount() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PersonEdit(
                    guid = route.guid
                )
            )
        )
    }

    fun onClickManagePasskey() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PasskeyList(
                    guid = route.guid
                )
            )
        )
    }

    fun onCreatePasskeyClick() {
        viewModelScope.launch {
            val passkeyCreated = createPasskeyUseCase?.invoke(
                username = uiState.value.selectedAccount?.person?.username ?: return@launch,
                rpId = uiState.value.selectedAccount?.account?.school?.rpId ?: return@launch
            )

            if (passkeyCreated != null) {
                when (passkeyCreated) {
                    is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                        val request = SavePersonPasskeyUseCase.Request(
                            authenticatedUserId = AuthenticatedUserPrincipalId(personGuid),
                            userGuid = personGuid,
                            passkeyWebAuthNResponse = passkeyCreated.authenticationResponseJSON,
                            deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
                        )
                        savePersonPasskeyUseCase(request)
                    }

                    is CreatePasskeyUseCase.Error -> {
                        _uiState.update { prev ->
                            prev.copy(
                                errorText = StringUiText(passkeyCreated.message ?: ""),
                            )
                        }
                    }

                    is CreatePasskeyUseCase.UserCanceledResult -> {
                        //do nothing
                    }
                }
            }

        }
    }

}