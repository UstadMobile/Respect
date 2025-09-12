package world.respect.shared.viewmodel.manageuser.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.oneroster.model.OneRosterGenderEnum
import world.respect.datalayer.respect.model.invite.RespectRedeemInviteRequest
import world.respect.datalayer.oneroster.rostering.model.OneRosterGenderEnum
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.shared.generated.resources.*
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class SignupUiState(
    val screenTitle: String = "",
    val actionBarButtonName: String = "",
    val nameLabel: String = "",
    val genderLabel: String = "",
    val dateOfBirthLabel: String = "",
    val personPicture: String?=null,

    val personInfo: RespectRedeemInviteRequest.PersonInfo? = null,


    val fullNameError: UiText? = null,
    val genderError: UiText? = null,
    val dateOfBirthError: UiText? = null
)


class SignupViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val route: SignupScreen = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = when (route.type) {
                ProfileType.PARENT, ProfileType.STUDENT -> SignupUiState(
                    screenTitle = getString(Res.string.your_profile_title),
                    actionBarButtonName = getString(Res.string.next),
                    nameLabel = getString(Res.string.your_name_label),
                    genderLabel = getString(Res.string.your_gender_label),
                    dateOfBirthLabel = getString(Res.string.your_dob_label),
                )

                ProfileType.CHILD -> SignupUiState(
                    screenTitle = getString(Res.string.child_profile_title),
                    actionBarButtonName = getString(Res.string.done),
                    nameLabel = getString(Res.string.child_name_label),
                    genderLabel = getString(Res.string.child_gender_label),
                    dateOfBirthLabel = getString(Res.string.child_dob_label),
                )
            }

            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = uiState.value.actionBarButtonName.asUiText(),
                        onClick = { onClickSave() }
                    ),
                    title = uiState.value.screenTitle.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }

    }
    fun onFullNameChanged(value: String) {
        _uiState.update { prev ->
            val currentPerson = prev.personInfo ?: RespectRedeemInviteRequest.PersonInfo()
            prev.copy(
                personInfo = currentPerson.copy(name = value),
                fullNameError = if (value.isNotBlank()) null else StringResourceUiText(Res.string.full_name_required)
            )
        }
    }

    fun onGenderChanged(value: OneRosterGenderEnum) {
        _uiState.update { prev ->
            val currentPerson = prev.personInfo ?: RespectRedeemInviteRequest.PersonInfo()
            prev.copy(
                personInfo = currentPerson.copy(gender = value),
                genderError = if (value != OneRosterGenderEnum.UNSPECIFIED) null else StringResourceUiText(Res.string.gender_required)
            )
        }
    }

    fun onDateOfBirthChanged(value: LocalDate?) {
        _uiState.update { prev ->
            val currentPerson = prev.personInfo ?: RespectRedeemInviteRequest.PersonInfo()
            prev.copy(
                personInfo = currentPerson.copy(dateOfBirth = value),
                dateOfBirthError = if (value != null)
                    null
                else
                    StringResourceUiText(Res.string.dob_required)
            )
        }
    }

    fun onPersonPictureChanged(pictureUri: String?) {
        _uiState.update { prev ->
            prev.copy(
                personPicture = pictureUri?:""
            )
        }

    }

    fun onClickSave() {

        viewModelScope.launch {
            val personInfo = _uiState.value.personInfo
            _uiState.update { prev ->
                prev.copy(
                    fullNameError = if (personInfo?.name.isNullOrEmpty()) StringResourceUiText(Res.string.full_name_required) else null,
                    genderError = if (personInfo?.gender?.value.isNullOrEmpty()) StringResourceUiText(
                        Res.string.gender_required
                    ) else null,
                    dateOfBirthError = if (personInfo?.dateOfBirth == null) StringResourceUiText(Res.string.dob_required) else null
                )
            }

            val hasError = listOf(
                personInfo?.name?.isBlank(),
                personInfo?.gender == OneRosterGenderEnum.UNSPECIFIED,
                personInfo?.dateOfBirth == null
            ).any { it == true }

            if (hasError) {
                return@launch
            } else {
                when (route.type) {
                    ProfileType.PARENT, ProfileType.STUDENT -> {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(CreateAccount.create(route.type,route.code,personInfo))
                            )
                    }
                    ProfileType.CHILD ->{
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(WaitingForApproval.create(route.type,route.code,route.uid?:""))
                            )
                    }
                }
            }
        }
    }
}

