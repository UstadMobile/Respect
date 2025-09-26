package world.respect.shared.viewmodel.person.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.fullName
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue

data class PersonDetailUiState(
    val person: DataLoadState<Person> = DataLoadingState(),
){
    val manageAccountVisible: Boolean
        get() = person.dataOrNull()?.username != null
}

class PersonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
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
                    visible = true,
                    text = Res.string.edit.asUiText(),
                    onClick = ::onClickEdit,
                    icon = FabUiState.FabIcon.EDIT,
                )
            )
        }

        viewModelScope.launch {
            schoolDataSource.personDataSource.findByGuidAsFlow(
                route.guid
            ).collect { person ->
                _appUiState.update { prev ->
                    prev.copy(
                        title = person.dataOrNull()?.fullName()?.asUiText(),
                    )
                }

                _uiState.update { prev ->
                    prev.copy(person = person)
                }
            }
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PersonEdit(route.guid))
        )
    }
    fun navigateToManageAccount() {
        uiState.value.person.dataOrNull().let {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ManageAccount.create(
                        guid = route.guid,
                        personName = it?.givenName ?: "",
                        personUsername = it?.username ?: return
                    )
                )
            )
        }
    }
}