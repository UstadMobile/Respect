package world.respect.shared.viewmodel.manageuser.otheroptionsignup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.other_options
import world.respect.shared.generated.resources.passkey_not_supported
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class OtherOptionsSignupUiState(
    val passkeyError: String? = null,
    val generalError: StringResourceUiText? = null
)

class OtherOptionsSignupViewModel(
    savedStateHandle: SavedStateHandle,
    private val createPasskeyUseCase: CreatePasskeyUseCase?,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {
    private val route: OtherOptionsSignup = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(OtherOptionsSignupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.other_options.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
        _uiState.update { prev ->
            prev.copy(
                generalError = if (createPasskeyUseCase == null)
                    StringResourceUiText(Res.string.passkey_not_supported)
                else null
            )
        }
    }


    fun onClickSignupWithPasskey() {
        viewModelScope.launch {
            try {
                val schoolDirEntry = respectAppDataSource.schoolDirectoryEntryDataSource
                    .getSchoolDirectoryEntryByUrl(route.schoolUrl).dataOrNull() ?: throw IllegalStateException()
                val rpId = schoolDirEntry.rpId
                val username = route.respectRedeemInviteRequest.account.username

                if (createPasskeyUseCase == null || rpId==null){
                    _uiState.update {
                        it.copy(
                            generalError = StringResourceUiText(Res.string.passkey_not_supported)
                        )
                    }
                }else {
                    val createPasskeyResult = createPasskeyUseCase(
                        username = username,
                        rpId = rpId
                    )

                    when (createPasskeyResult) {
                        //This is quite wrong...
                        is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                            val redeemInviteRequest = route.respectRedeemInviteRequest
                            val account = RespectRedeemInviteRequest.Account(
                                username = username,
                                credential = RespectPasskeyCredential(
                                    createPasskeyResult.authenticationResponseJSON
                                )
                            )

                            val updatedRedeemInviteRequest = RespectRedeemInviteRequest(
                                code = redeemInviteRequest.code,
                                classUid = redeemInviteRequest.classUid,
                                role = redeemInviteRequest.role,
                                accountPersonInfo = redeemInviteRequest.accountPersonInfo,
                                parentOrGuardianRole = redeemInviteRequest.parentOrGuardianRole,
                                account = account
                            )
                        }

                        is CreatePasskeyUseCase.Error -> {
                            _uiState.update { prev ->
                                prev.copy(
                                    passkeyError = createPasskeyResult.message,
                                )
                            }
                        }

                        is CreatePasskeyUseCase.UserCanceledResult -> {
                            // do nothing
                        }
                    }
                }

            } catch (e: Exception) {
                println(e.message.toString())
            }
        }
    }

    fun onClickSignupWithPassword() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnterPasswordSignup.create(
                    schoolUrl = route.schoolUrl,
                    inviteRequest = route.respectRedeemInviteRequest
                )
            )
        )
    }

    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(HowPasskeyWorks))
    }
}