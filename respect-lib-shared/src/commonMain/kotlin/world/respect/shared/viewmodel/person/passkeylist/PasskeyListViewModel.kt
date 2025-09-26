package world.respect.shared.viewmodel.person.passkeylist
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.datalayer.db.opds.entities.PersonPasskeyEntity
import world.respect.datalayer.shared.paging.EmptyPagingSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.passkeys
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class PasskeyListUiState(
    val passkeys: IPagingSourceFactory<Int, PersonPasskeyEntity> = IPagingSourceFactory {
        EmptyPagingSource()
    },
    val showRevokePasskeyDialog : Boolean = false,
    val personPasskeyUid : Long = 0
)


class PasskeyListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val json: Json,
    private val createPasskeyUseCase: CreatePasskeyUseCase?,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireSelectedAccountScope()

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
            )
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
    fun onClickRevokePasskey(personPasskeyUid: Long){
        _uiState.update { prev ->
            prev.copy(
                showRevokePasskeyDialog = true,
                personPasskeyUid = personPasskeyUid
            )
        }
    }


    companion object {

        const val DEST_NAME = "PasskeyList"


    }



}
