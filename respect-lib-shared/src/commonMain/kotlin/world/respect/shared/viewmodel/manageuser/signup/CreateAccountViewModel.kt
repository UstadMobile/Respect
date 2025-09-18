package world.respect.shared.viewmodel.manageuser.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.credentials.passkey.VerifyDomainUseCase
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.shared.domain.account.createinviteredeemrequest.RespectRedeemInviteRequestUseCase
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.account.invite.RedeemInviteUseCase
import world.respect.shared.domain.account.signup.SignupCredential
import world.respect.shared.domain.account.signup.SignupUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.passkey_not_supported
import world.respect.shared.generated.resources.username_required
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

data class CreateAccountViewModelUiState(
    val username: String = "",
    val usernameError: StringResourceUiText? = null,
    val generalError: StringResourceUiText? = null,
    val signupError: String? = null,
    val inviteInfo: RespectInviteInfo? = null,
    val passkeySupported : Boolean =true
)

class CreateAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val verifyDomainUseCase: VerifyDomainUseCase,
    private val createPasskeyUseCase: CreatePasskeyUseCase?,
    private val submitRedeemInviteRequestUseCase: RedeemInviteUseCase,
    private val respectRedeemInviteRequestUseCase: RespectRedeemInviteRequestUseCase,
    private val signupUseCase: SignupUseCase,
    private val inviteInfoUseCase: GetInviteInfoUseCase
) : RespectViewModel(savedStateHandle) {
    private val route: CreateAccount = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(CreateAccountViewModelUiState())

    val uiState = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            val inviteInfo = inviteInfoUseCase(route.code)

            _appUiState.update {
                it.copy(
                    title = Res.string.create_account.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
            _uiState.update { prev ->
                prev.copy(
                    inviteInfo = inviteInfo,
                    passkeySupported = createPasskeyUseCase != null && inviteInfo.school.rpId != null
                            && verifyDomainUseCase(inviteInfo.school.rpId ?: ""),
                    generalError = if (!(createPasskeyUseCase != null && inviteInfo.school.rpId != null))
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
            if (inviteInfo==null) throw IllegalStateException("inviteInfo is null")
            val username = _uiState.value.username

            _uiState.update {
                it.copy(
                    usernameError = if (username.isBlank()) StringResourceUiText(Res.string.username_required) else null
                )
            }

            if (username.isBlank()) return@launch

            try {


                if (createPasskeyUseCase==null||inviteInfo.school.rpId==null){
                    when (route.type) {
                        ProfileType.CHILD , ProfileType.STUDENT->{
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(EnterPasswordSignup.create(username,route.type,route.code,route.personInfo))
                            )
                        }
                        ProfileType.PARENT ->{
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(EnterPasswordSignup.create(username,ProfileType.CHILD,route.code,route.personInfo))
                            )
                        }
                    }
                }else{

                    val createPasskeyResult = createPasskeyUseCase(
                        username = username,
                        rpId = inviteInfo.school.rpId?:""
                    )
                    when (createPasskeyResult) {
                        is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                            //i forgot why i created this
                            val signupCredential = SignupCredential.Passkey(
                                username = username,
                                authenticationResponseJSON = createPasskeyResult.authenticationResponseJSON
                            )

                            sendSignupCredential(signupCredential)
                            when (route.type) {
                                ProfileType.CHILD ->{
                                    //ignore not create account for child
                                }
                                ProfileType.STUDENT->{
                                    val redeemRequest = respectRedeemInviteRequestUseCase(
                                        inviteInfo = inviteInfo,
                                        username = username,
                                        personInfo = route.personInfo,
                                        parentOrGuardian = null,
                                        credential = RespectRedeemInviteRequest.RedeemInvitePasskeyCredential(
                                            createPasskeyResult.authenticationResponseJSON
                                        )
                                    )
//                                    val result = submitRedeemInviteRequestUseCase(redeemRequest)
//
//                                    _navCommandFlow.tryEmit(
//                                        NavCommand.Navigate(WaitingForApproval.create(route.type,route.code,result?.guid?:""))
//                                    )
                                }
                                ProfileType.PARENT ->{
                                    _navCommandFlow.tryEmit(
                                        NavCommand.Navigate(
                                            SignupScreen.create(
                                                profileType = ProfileType.CHILD,
                                                inviteCode = route.code,
                                                parentPersonInfoJson = route.personInfo,
                                                parentUsername = username,
                                                parentRedeemCredential = RespectRedeemInviteRequest.RedeemInvitePasskeyCredential(
                                                    createPasskeyResult.authenticationResponseJSON
                                                )
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        is CreatePasskeyUseCase.Error -> {
                            _uiState.update { prev ->
                                prev.copy(
                                    signupError = createPasskeyResult.message,
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
    private fun sendSignupCredential(credential: SignupCredential) {
        viewModelScope.launch {
            try {
                signupUseCase(credential)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(signupError = "${e.message}")
                }
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
                usernameError = if (username.isBlank()) StringResourceUiText(Res.string.username_required) else null
            )
        }

        if (username.isBlank()) return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OtherOptionsSignup.create(
                    username = uiState.value.username,
                    profileType = route.type,
                    inviteCode = route.code,
                    personInfo = route.personInfo
                )
            )
        )
    }

}
