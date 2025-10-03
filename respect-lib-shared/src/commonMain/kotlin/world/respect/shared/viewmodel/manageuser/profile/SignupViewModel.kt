package world.respect.shared.viewmodel.manageuser.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.java.KoinJavaComponent.getKoin
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.domain.account.child.AddChildAccountUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.child_dob_label
import world.respect.shared.generated.resources.child_gender_label
import world.respect.shared.generated.resources.child_name_label
import world.respect.shared.generated.resources.child_profile_title
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.your_dob_label
import world.respect.shared.generated.resources.your_gender_label
import world.respect.shared.generated.resources.your_name_label
import world.respect.shared.generated.resources.your_profile_title
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState

data class SignupUiState(
    val screenTitle: UiText? = null,
    val actionBarButtonName: UiText? = null,
    val nameLabel: UiText? = null,
    val genderLabel: UiText? = null,
    val dateOfBirthLabel: UiText?=null,
    val personPicture: String?=null,

    val personInfo: RespectRedeemInviteRequest.PersonInfo = RespectRedeemInviteRequest.PersonInfo(),


    val fullNameError: UiText? = null,
    val genderError: UiText? = null,
    val dateOfBirthError: UiText? = null
)


class SignupViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle) {

    private val route: SignupScreen = savedStateHandle.toRoute()

    val schoolScopeId = SchoolDirectoryEntryScopeId(
        route.schoolUrl, null,
    )
    val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
        schoolScopeId.scopeId
    )

    val addChildAccountUseCase : AddChildAccountUseCase = schoolScope.get()
    private val _uiState = MutableStateFlow(SignupUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { prev ->
            when(route.type) {
                ProfileType.CHILD -> {
                    prev.copy(
                        screenTitle = Res.string.child_profile_title.asUiText(),
                        actionBarButtonName = Res.string.done.asUiText(),
                        nameLabel = Res.string.child_name_label.asUiText(),
                        genderLabel = Res.string.child_gender_label.asUiText(),
                        dateOfBirthLabel = Res.string.child_dob_label.asUiText(),
                    )
                }

                else -> {
                    prev.copy(
                        screenTitle = Res.string.your_profile_title.asUiText(),
                        actionBarButtonName = Res.string.next.asUiText(),
                        nameLabel = Res.string.your_name_label.asUiText(),
                        genderLabel = Res.string.your_gender_label.asUiText(),
                        dateOfBirthLabel = Res.string.your_dob_label.asUiText(),
                    )
                }


            }
        }

        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = uiState.value.actionBarButtonName,
                        onClick = { onClickSave() }
                    ),
                    title = uiState.value.screenTitle,
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }

    }
    fun onFullNameChanged(value: String) {
        _uiState.update { prev ->
            val currentPerson = prev.personInfo
            prev.copy(
                personInfo = currentPerson.copy(name = value),
                fullNameError = if (value.isNotBlank()) null else StringResourceUiText(Res.string.required)
            )
        }
    }


    fun onGenderChanged(value: PersonGenderEnum) {
        _uiState.update { prev ->
            val currentPerson = prev.personInfo
            prev.copy(
                personInfo = currentPerson.copy(gender = value),
                genderError = if (value != PersonGenderEnum.UNSPECIFIED) null else StringResourceUiText(Res.string.required)
            )
        }
    }

    fun onDateOfBirthChanged(value: LocalDate?) {
        if(value == null)
            return

        _uiState.update { prev ->
            val currentPerson = prev.personInfo
            prev.copy(
                personInfo = currentPerson.copy(dateOfBirth = value),
                dateOfBirthError = null
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
                    fullNameError = if (personInfo.name.isEmpty()) StringResourceUiText(Res.string.required) else null,
                    genderError = if (personInfo.gender.value.isEmpty()) StringResourceUiText(
                        Res.string.required
                    ) else null
                )
            }

            val hasError = listOf(
                personInfo.name.isBlank(),
                //personInfo?.gender == PersonGenderEnum.UNSPECIFIED,
                //personInfo?.dateOfBirth == null
            ).any { it }

            if (hasError) {
                return@launch
            } else {
                when (route.type) {
                    ProfileType.CHILD -> {
                        addChildAccountUseCase.invoke(personInfo)

                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                destination = WaitingForApproval(),
                                clearBackStack = true,
                            )
                        )
                    }

                    else -> {
                        _navCommandFlow.tryEmit(
                            value = NavCommand.Navigate(
                                destination = CreateAccount.create(
                                    profileType = route.type,
                                    schoolUrl = route.schoolUrl,
                                    inviteRequest = route.respectRedeemInviteRequest.copy(
                                        accountPersonInfo = personInfo
                                    )
                                )
                            )
                        )
                     }
                }
            }
        }
    }
}

