package world.respect.shared.viewmodel.person.deleteaccount

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.viewmodel.RespectViewModel

data class DeleteAccountUiState(
    val accountGuid: String = "",
)
class DeleteAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val json: Json,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val _uiState = MutableStateFlow(
        DeleteAccountUiState(
            accountGuid = ""
        )
    )

    val uiState = _uiState.asStateFlow()
    fun onDeleteAccount(){

    }


}
