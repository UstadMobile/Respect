package world.respect.shared.viewmodel.manageuser.profile

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.scope.Scope
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.child.AddChildAccountUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest.Companion.DATE_OF_BIRTH_EPOCH
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.child_dob_label
import world.respect.shared.generated.resources.child_gender_label
import world.respect.shared.generated.resources.child_name_label
import world.respect.shared.generated.resources.child_profile_title
import world.respect.shared.generated.resources.date_of_birth_in_future
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.required_field
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
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.manageuser.signup.SignupScreenModeEnum
import kotlin.time.Clock

data class SignupUiState(
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
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle)  {

    private val route: SignupScreen = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(SignupUiState())

    val uiState = _uiState.asStateFlow()

    init {
        Napier.d("SignupViewModel: init: route type=${route.signupMode.value}")

        _uiState.update { prev ->
            when(route.signupMode) {
                SignupScreenModeEnum.ADD_CHILD_TO_PARENT -> {
                    prev.copy(
                        nameLabel = Res.string.child_name_label.asUiText(),
                        genderLabel = Res.string.child_gender_label.asUiText(),
                        dateOfBirthLabel = Res.string.child_dob_label.asUiText(),
                    )
                }

                else -> {
                    prev.copy(
                        nameLabel = Res.string.your_name_label.asUiText(),
                        genderLabel = Res.string.your_gender_label.asUiText(),
                        dateOfBirthLabel = Res.string.your_dob_label.asUiText(),
                    )
                }


            }
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = when(route.signupMode) {
                        SignupScreenModeEnum.ADD_CHILD_TO_PARENT  -> Res.string.done.asUiText()
                        else -> Res.string.next.asUiText()
                    },
                    onClick = ::onClickSave
                ),
                title = when(route.signupMode) {
                    SignupScreenModeEnum.ADD_CHILD_TO_PARENT -> Res.string.child_profile_title.asUiText()
                    else -> Res.string.your_profile_title.asUiText()
                },
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onFullNameChanged(value: String) {
        _uiState.update { prev ->
            val currentPerson = prev.personInfo
            prev.copy(
                personInfo = currentPerson.copy(name = value),
                fullNameError = if (value.isNotBlank()) null else StringResourceUiText(Res.string.required_field)
            )
        }
    }


    fun onGenderChanged(value: PersonGenderEnum) {
        _uiState.update { prev ->
            val currentPerson = prev.personInfo
            prev.copy(
                personInfo = currentPerson.copy(gender = value),
                genderError = if (value != PersonGenderEnum.UNSPECIFIED) null else StringResourceUiText(Res.string.required_field)
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
        Napier.d("SignupViewModel: onClickSave: route type=")
        launchWithLoadingIndicator {
            Napier.d("SignupViewModel: onClickSave.launch: name=${_uiState.value.personInfo.name}")
            val personInfo = _uiState.value.personInfo
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            _uiState.update { prev ->
                prev.copy(
                    fullNameError = if (personInfo.name.isEmpty()) StringResourceUiText(Res.string.required_field) else null,
                    genderError = if (personInfo.gender == PersonGenderEnum.UNSPECIFIED) StringResourceUiText(
                        Res.string.required_field) else null,
                    dateOfBirthError = if (personInfo.dateOfBirth == DATE_OF_BIRTH_EPOCH) {
                        StringResourceUiText(Res.string.required_field)
                    } else if (personInfo.dateOfBirth > today) {
                        StringResourceUiText(Res.string.date_of_birth_in_future)
                    } else null

                )
            }

            val hasError = listOf(
                personInfo.name.isBlank(),
                personInfo.dateOfBirth > today,
                personInfo.gender == PersonGenderEnum.UNSPECIFIED,
                personInfo.dateOfBirth == DATE_OF_BIRTH_EPOCH
            ).any { it }

            if (hasError) {
                Napier.w("SignupViewModel: onClickSave.launch: haserrors")
                return@launchWithLoadingIndicator
            } else {
                when (route.signupMode) {
                    SignupScreenModeEnum.ADD_CHILD_TO_PARENT -> {
                        Napier.d("SignupViewModel: adding child")
                        val scope: Scope = accountManager.requireActiveAccountScope()
                        val addChildAccountUseCase: AddChildAccountUseCase = scope.get()

                        /*
                        addChildAccountUseCase(
                            personInfo = personInfo,
                            parentUsername = route.respectRedeemInviteRequest.account.username,
                            classUid = route.respectRedeemInviteRequest.classUid ?: "",
                            inviteCode = route.respectRedeemInviteRequest.code,
                            familyPersonGuid = route.respectRedeemInviteRequest.invite.forFamilyOfGuid,
                        )*/

                        Napier.d("SignupViewModel: Navigating to wait for approval")
                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                destination = WaitingForApproval(),
                                clearBackStack = true,
                            )
                        )
                    }

                    else -> {
                        Napier.d("SignupViewModel: Navigating to create account")
                        _navCommandFlow.tryEmit(
                            value = NavCommand.Navigate(
                                destination = CreateAccount.create(
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

