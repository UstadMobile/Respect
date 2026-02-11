package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.school
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SharedDevicesSettings
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.getTitle
import kotlin.getValue

data class SchoolSettingsUiState(
    val schoolName: String? = null,
    val error: UiText? = null,
    val sharedSchoolDeviceCount: Int? = null,
)

class SchoolSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val respectAppDataSource: RespectAppDataSource,
    ) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(SchoolSettingsUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.school.asUiText(),
                hideBottomNavigation = true,
            )
        }
        viewModelScope.launch {
            accountManager.accounts.combine(
                accountManager.selectedAccountFlow
            ) { storedAccounts, activeAccount ->
                Pair(storedAccounts, activeAccount)
            }.collectLatest { (storedAccounts, activeAccount) ->
                _uiState.update { prev ->
                    prev.copy(
                        schoolName =  activeAccount?.school?.name?.getTitle()
                    )
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.personDataSource.listAsFlow(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    includeRelated = true,
                )
            ).combine(accountManager.selectedAccountAndPersonFlow) { person, activeAccount ->
                Pair(person, activeAccount)
            }.collect { (personsResult, activeAccount) ->
                val sharedDevices = personsResult.dataOrNull()?.filter { person ->
                    // Filter for shared school devices
                    person.roles.any { role ->
                        role.roleEnum == PersonRoleEnum.SHARED_SCHOOL_DEVICE
                    }
                }

                _uiState.update { prev ->
                    prev.copy(
                        sharedSchoolDeviceCount = sharedDevices?.size
                    )
                }
            }
        }
    }

    fun onClickSharedSchoolDevices() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SharedDevicesSettings)
        )
    }
}