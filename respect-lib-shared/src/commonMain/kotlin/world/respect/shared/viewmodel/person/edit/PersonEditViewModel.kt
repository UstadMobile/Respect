package world.respect.shared.viewmodel.person.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.isReadyAndSettled
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.phonenumber.PhoneNumValidatorUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_person
import world.respect.shared.generated.resources.date_of_birth_in_future
import world.respect.shared.generated.resources.edit_person
import world.respect.shared.generated.resources.invalid
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.getValue
import kotlin.time.Clock

data class PersonEditUiState(
    val person: DataLoadState<Person> = DataLoadingState(),
    val dateOfBirthError: UiText? = null,

    /**
     * Used to determine if the user has actually set a phone number. This is set by the UI
     * components as a user inputs a number. True if the national phone number part (e.g. not just
     * country code) is set, false otherwise.
     *
     * A person without any phone number set is allowed, but if a number is entered, it will be
     * validated.
     */
    val nationalPhoneNumSet: Boolean = false,
    val phoneNumError: UiText? = null,
) {
    val fieldsEnabled : Boolean
        get() = person.isReadyAndSettled()

    val hasErrors: Boolean
        get() = dateOfBirthError != null || phoneNumError != null
}

class PersonEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
    private val phoneNumValidatorUseCase: PhoneNumValidatorUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    val route: PersonEdit = savedStateHandle.toRoute()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val guid = route.guid ?: schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        Person.TABLE_ID
    ).toString()

    private val _uiState = MutableStateFlow(PersonEditUiState())

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if (route.guid == null)
                    Res.string.add_person.asUiText()
                else
                    Res.string.edit_person.asUiText(),
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    onClick = ::onClickSave,
                    text = Res.string.save.asUiText(),
                    visible = true,
                )
            )
        }

        launchWithLoadingIndicator {
            if (route.guid != null) {
                loadEntity(
                    json = json,
                    serializer = Person.serializer(),
                    loadFn = { params ->
                        schoolDataSource.personDataSource.findByGuid(params, guid)
                    },
                    uiUpdateFn = { person ->
                        _uiState.update { prev -> prev.copy(person = person) }
                    }
                )
            } else {
                _uiState.update { prev ->
                    prev.copy(
                        person = DataReadyState(
                            Person(
                                guid = guid,
                                givenName = "",
                                familyName = "",
                                roles = emptyList(),
                                gender = PersonGenderEnum.UNSPECIFIED
                            )
                        )
                    )
                }
            }
        }
    }

    fun onEntityChanged(person: Person) {
        val personToCommit = _uiState.updateAndGet { prev ->
            val prevPerson = prev.person.dataOrNull()

            prev.copy(
                person = DataReadyState(person),
                phoneNumError = if(prev.phoneNumError != null && prevPerson?.phoneNumber == person.phoneNumber) {
                    prev.phoneNumError
                }else {
                    null
                }
            )
        }.person.dataOrNull() ?: return

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(personToCommit)
        }
    }

    fun onNationalPhoneNumSetChanged(phoneNumSet: Boolean) {
        _uiState.takeIf { it.value.nationalPhoneNumSet != phoneNumSet }?.update { prev ->
            prev.copy(nationalPhoneNumSet = phoneNumSet)
        }
    }

    fun onClickSave() {
        val person = _uiState.value.person.dataOrNull()?.copy(
            lastModified = Clock.System.now(),
        ) ?: return

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val dob = person.dateOfBirth

        _uiState.update { prev ->
            prev.copy(
                dateOfBirthError = if(dob != null && dob > today) {
                    Res.string.date_of_birth_in_future.asUiText()
                }else {
                    null
                },
                phoneNumError = if(uiState.value.nationalPhoneNumSet &&
                    !phoneNumValidatorUseCase.isValid(person.phoneNumber ?: "")
                ) {
                    Res.string.invalid.asUiText()
                }else {
                    null
                }
            )
        }


        if(uiState.value.hasErrors)
            return

        launchWithLoadingIndicator {
            try {
                schoolDataSource.personDataSource.store(listOf(person))

                if (route.guid == null) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            PersonDetail(guid), popUpTo = route, popUpToInclusive = true
                        )
                    )
                } else {
                    _navCommandFlow.tryEmit(NavCommand.PopUp())
                }
            } catch (_: Throwable) {
                //needs to display snack bar here
            }
        }
    }
}
