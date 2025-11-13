package world.respect.shared.viewmodel.person.list

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
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.shared.paging.EmptyPagingSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.ext.resultExpected
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.people
import world.respect.shared.generated.resources.person
import world.respect.shared.generated.resources.select_person
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isAdminOrTeacher
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.shared.viewmodel.app.appstate.FabUiState


data class PersonListUiState(
    val persons: IPagingSourceFactory<Int, PersonListDetails> = IPagingSourceFactory {
        EmptyPagingSource()
    },
    val showAddPersonItem: Boolean = false,
    val showInviteCode: String? = null,
)

class PersonListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val resultReturner: NavResultReturner,
    private val setClipboardStringUseCase: SetClipboardStringUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(PersonListUiState())

    val uiState = _uiState.asStateFlow()

    private val launchDebounced = LaunchDebouncer(viewModelScope)

    private val route: PersonList = savedStateHandle.toRoute()

    private val pagingSourceFactoryHolder = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listDetailsAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByName = _appUiState.value.searchState.searchText.takeIf { it.isNotBlank() },
                filterByPersonRole = route.filterByRole,
            )
        )
    }

    init {
        _uiState.takeIf { route.showInviteCode!= null }
            ?.update { it.copy(showInviteCode = route.showInviteCode) }

        _appUiState.update {
            it.copy(
                title = if(!route.resultExpected) {
                    Res.string.people.asUiText()
                }else {
                    Res.string.select_person.asUiText()
                },
                fabState = it.fabState.copy(
                    onClick = ::onClickAdd,
                    text = Res.string.person.asUiText(),
                    icon = FabUiState.FabIcon.ADD,
                ),
                searchState = AppBarSearchUiState(
                    visible = true,
                    searchText = "",
                    onSearchTextChanged = ::onSearchTextChanged
                ),
                showBackButton = route.resultExpected,
                hideBottomNavigation = route.resultExpected,
                userAccountIconVisible = !route.resultExpected,
            )
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selectedAcct ->
                val canAddPerson = selectedAcct?.person?.isAdminOrTeacher() == true
                _appUiState.update { prev ->
                    prev.copy(
                        fabState = prev.fabState.copy(
                            visible = canAddPerson && !route.resultExpected
                        )
                    )
                }

                _uiState.update {
                    it.copy(showAddPersonItem = canAddPerson && route.resultExpected)
                }
            }
        }

        _uiState.update {
            it.copy(persons = pagingSourceFactoryHolder)
        }
    }

    fun onSearchTextChanged(text: String) {
        _appUiState.update {
            it.copy(
                searchState = it.searchState.copy(
                    searchText = text
                )
            )
        }

        launchDebounced.launch("") {
            pagingSourceFactoryHolder.invalidate()
        }
    }

    fun onClickItem(person: PersonListDetails) {
        viewModelScope.launch {
            val personSelected = schoolDataSource.personDataSource.findByGuid(
                loadParams = DataLoadParams(),
                guid = person.guid,
            ).dataOrNull()

            if(
                !resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = personSelected,
                )
            ) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(PersonDetail(person.guid))
                )
            }
        }
    }

    fun onClickAdd() {
        print(""+route.filterByRole)
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PersonEdit.create(
                    null,
                    resultDest = route.resultDest,
                    filterByRole = route.filterByRole
                )
            )
        )
    }

    fun onClickInviteCode() {
        _uiState.value.showInviteCode?.also {
            setClipboardStringUseCase(it)
        }
    }

}