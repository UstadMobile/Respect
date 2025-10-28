package world.respect.shared.viewmodel.manageuser.waitingforapproval

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.waiting_title
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class WaitingForApprovalUiState(
    val className: String = "",
    val isRefreshing: Boolean = false
)

class WaitingForApprovalViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val _uiState = MutableStateFlow(WaitingForApprovalUiState())

    private val schoolDataSource: SchoolDataSource by inject()

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.waiting_title.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = true,
                showBackButton = false,
            )
        }

        viewModelScope.launch {
            val activeUserUid = accountManager.selectedAccount?.userGuid ?: return@launch

            while(true) {
                val personLoaded = schoolDataSource.personDataSource
                    .findByGuid(DataLoadParams(), activeUserUid)
                if(personLoaded.dataOrNull()?.status == PersonStatusEnum.ACTIVE) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(RespectAppLauncher())
                    )
                    return@launch
                }

                delay(2_000)
            }
        }
    }
}
