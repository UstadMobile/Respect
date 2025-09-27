package world.respect.shared.viewmodel.person.passkeylist
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.passkeys
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class PasskeyListUiState(
    val passkeys: DataLoadState<List<PersonPasskey>> = DataLoadingState(),
    val showRevokePasskeyDialog : Boolean = false,
    val passkeyPendingRevocation : PersonPasskey? = null,
)


class PasskeyListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
    private val createPasskeyUseCase: CreatePasskeyUseCase?,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(
        PasskeyListUiState()
    )

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.passkeys.asUiText(),
                userAccountIconVisible = false,
                navigationVisible = true,
                hideBottomNavigation = true,
            )
        }

        viewModelScope.launch {
            schoolDataSource.personPasskeyDataSource.listAllAsFlow().collect {
                _uiState.update { prev ->
                    prev.copy(
                        passkeys = it
                    )
                }
            }
        }
    }

    fun onDismissRevokePasskeyDialog(){
       _uiState.update { prev ->
           prev.copy(
               showRevokePasskeyDialog = false
           )
       }
    }


    fun revokePasskey() {

    }

    fun onClickRevokePasskey(passkey: PersonPasskey){
        _uiState.update { prev ->
            prev.copy(
                showRevokePasskeyDialog = true,
                passkeyPendingRevocation = passkey,
            )
        }
    }


    companion object {

        const val DEST_NAME = "PasskeyList"


    }



}
