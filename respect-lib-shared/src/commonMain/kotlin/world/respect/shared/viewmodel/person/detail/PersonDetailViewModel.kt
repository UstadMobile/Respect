package world.respect.shared.viewmodel.person.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.phonenumber.OnClickPhoneNumUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.navigation.SetUsernameAndPassword
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.fullName
import world.respect.shared.util.ext.isAdmin
import world.respect.shared.util.ext.isAdminOrTeacher
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue

data class PersonDetailUiState(
    val person: DataLoadState<Person> = DataLoadingState(),
    val manageAccountVisible: Boolean = false,
    val createAccountVisible: Boolean = false,
)

class PersonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val onClickPhoneNumUseCase: OnClickPhoneNumUseCase? = null,
) : RespectViewModel(savedStateHandle), KoinScopeComponent{

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: PersonDetail = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(PersonDetailUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                fabState = FabUiState(
                    text = Res.string.edit.asUiText(),
                    onClick = ::onClickEdit,
                    icon = FabUiState.FabIcon.EDIT,
                )
            )
        }

        viewModelScope.launch {
            schoolDataSource.personDataSource.findByGuidAsFlow(
                route.guid
            ).combine(accountManager.selectedAccountAndPersonFlow) { person, activeAccount ->
                Pair(person, activeAccount)
            }.collect { (person, activeAccount) ->
                val personVal = person.dataOrNull()
                val hasAccountPermission = activeAccount?.person?.isAdmin() == true
                        || activeAccount?.person?.guid == person.dataOrNull()?.guid
                val personRole = personVal?.roles?.firstOrNull()?.roleEnum

                val canEdit = hasAccountPermission ||
                        (personRole in listOf(PersonRoleEnum.STUDENT, PersonRoleEnum.PARENT)
                                && activeAccount?.person?.isAdminOrTeacher() == true)

                _appUiState.update { prev ->
                    prev.copy(
                        title = person.dataOrNull()?.fullName()?.asUiText(),
                        fabState = prev.fabState.copy(
                            visible = canEdit,
                        )
                    )
                }

                _uiState.update { prev ->
                    prev.copy(
                        person = person,
                        manageAccountVisible = hasAccountPermission && personVal?.username != null,
                        createAccountVisible = personVal != null &&
                                activeAccount?.person?.isAdminOrTeacher() == true &&
                                personVal.username == null
                    )
                }
            }
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PersonEdit.create(route.guid))
        )
    }

    fun onClickCreateAccount() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SetUsernameAndPassword(route.guid))
        )
    }

    fun navigateToManageAccount() {
        uiState.value.person.dataOrNull().let {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ManageAccount(guid = route.guid)
                )
            )
        }
    }

    fun onClickPhoneNumber() {
        uiState.value.person.dataOrNull()?.phoneNumber?.also { phoneNum ->
            onClickPhoneNumUseCase?.invoke(phoneNum)
        }
    }

}