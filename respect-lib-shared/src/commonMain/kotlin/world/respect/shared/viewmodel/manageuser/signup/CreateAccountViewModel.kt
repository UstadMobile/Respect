package world.respect.shared.viewmodel.manageuser.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.credentials.passkey.VerifyDomainUseCase
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.passkey_not_supported
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class CreateAccountViewModelUiState(
    val username: String = "",
    val usernameError: UiText? = null,
    val generalError: UiText? = null,
    val signupError: UiText? = null,
    val inviteInfo: RespectInviteInfo? = null,
    val passkeySupported : Boolean = false,
)

class CreateAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val verifyDomainUseCase: VerifyDomainUseCase,
    private val createPasskeyUseCase: CreatePasskeyUseCase?,
    private val respectAppDataSource: RespectAppDataSource,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    private val route: CreateAccount = savedStateHandle.toRoute()

    override val scope: Scope
        get() = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
        )

    private val inviteInfoUseCase: GetInviteInfoUseCase by inject()

    private val _uiState = MutableStateFlow(CreateAccountViewModelUiState())

    val uiState = _uiState.asStateFlow()

    private val passkeySupported = CompletableDeferred<Boolean>()

    private val schoolDirectoryEntry = CompletableDeferred<SchoolDirectoryEntry>()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.create_account.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }

        viewModelScope.launch {
            val inviteInfo = inviteInfoUseCase(route.respectRedeemInviteRequest.code)
            val schoolDirEntryVal = respectAppDataSource.schoolDirectoryEntryDataSource
                .getSchoolDirectoryEntryByUrl(route.schoolUrl).dataOrNull()?.also {
                    schoolDirectoryEntry.complete(it)
                } ?: throw IllegalStateException()

            val rpId = schoolDirEntryVal.rpId
            val passkeySupportedVal = createPasskeyUseCase != null &&
                    rpId != null &&
                    verifyDomainUseCase(rpId)

            passkeySupported.complete(passkeySupportedVal)

            _uiState.update { prev ->
                prev.copy(
                    inviteInfo = inviteInfo,
                    passkeySupported = passkeySupportedVal,
                    generalError = if (!(createPasskeyUseCase != null && rpId != null))
                        StringResourceUiText(Res.string.passkey_not_supported)
                    else null
                )
            }
        }
    }

    fun onUsernameChanged(newValue: String) {
        _uiState.update {
            it.copy(
                username = newValue,
                usernameError = null,
                generalError = null
            )
        }
    }

    fun onClickSignupWithPasskey() {
        viewModelScope.launch {
            val inviteInfo = uiState.value.inviteInfo

            if (inviteInfo == null)
                throw IllegalStateException("inviteInfo is null")

            val username = _uiState.value.username

            _uiState.update {
                it.copy(
                    usernameError = if (username.isBlank())
                        Res.string.required.asUiText()
                    else
                        null
                )
            }

            if (username.isBlank()) return@launch

            val rpIdVal = schoolDirectoryEntry.await().rpId
            try {
                if (createPasskeyUseCase != null && rpIdVal != null && passkeySupported.await()) {
                    val createPasskeyResult = createPasskeyUseCase(
                        username = username,
                        rpId = rpIdVal
                    )

                    when (createPasskeyResult) {
                        is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                            val redeemRequest = route.respectRedeemInviteRequest.copy(
                                account = RespectRedeemInviteRequest.Account(
                                    username = username,
                                    credential = RespectRedeemInviteRequest.RedeemInvitePasskeyCredential(
                                        authResponseJson = createPasskeyResult.authenticationResponseJSON
                                    )
                                )
                            )

                            accountManager.register(
                                redeemInviteRequest = redeemRequest,
                                schoolUrl = route.schoolUrl
                            )

                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    destination = if(
                                        route.respectRedeemInviteRequest.role == PersonRoleEnum.PARENT
                                    ) {
                                        SignupScreen.create(
                                            schoolUrl = route.schoolUrl,
                                            profileType = ProfileType.CHILD,
                                            inviteRequest = redeemRequest
                                        )
                                    }else {
                                        WaitingForApproval()
                                    }
                                )
                            )
                        }

                        is CreatePasskeyUseCase.Error -> {
                            _uiState.update { prev ->
                                prev.copy(
                                    signupError = createPasskeyResult.message?.asUiText()
                                        ?: Res.string.something_went_wrong.asUiText(),
                                )
                            }
                        }

                        is CreatePasskeyUseCase.UserCanceledResult -> {
                            // do nothing
                        }
                    }
                }else{
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            EnterPasswordSignup.create(
                                schoolUrl = route.schoolUrl,
                                inviteRequest = route.respectRedeemInviteRequest.copy(
                                    account = RespectRedeemInviteRequest.Account(
                                        username = username,
                                        credential =route.respectRedeemInviteRequest.account.credential,
                                    )
                                )
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        signupError = e.getUiTextOrGeneric()
                    )
                }
                println(e.message.toString())
            }
        }
    }

    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(HowPasskeyWorks)
        )
    }

    fun onOtherOptionsClick() {
        val username = _uiState.value.username

        _uiState.update {
            it.copy(
                usernameError = if (username.isBlank()) StringResourceUiText(Res.string.required) else null
            )
        }

        if (username.isBlank()) return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OtherOptionsSignup.create(
                    schoolUrl = route.schoolUrl,
                    inviteRequest = route.respectRedeemInviteRequest.copy(
                        account = RespectRedeemInviteRequest.Account(
                            username = username,
                            credential =route.respectRedeemInviteRequest.account.credential,
                        )
                    )
                )
            )
        )
    }

}
