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
import world.respect.shared.generated.resources.add_new_person
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.generated.resources.people
import world.respect.shared.generated.resources.select_person
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase.PermissionsRequiredByRole
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import world.respect.shared.domain.permissions.CheckSchoolPermissionsUseCase
import world.respect.shared.viewmodel.app.appstate.ExpandableFabIcon
import world.respect.shared.viewmodel.app.appstate.ExpandableFabItem
import world.respect.shared.viewmodel.app.appstate.ExpandableFabUiState


data class PersonListUiState(
    val persons: IPagingSourceFactory<Int, PersonListDetails> = IPagingSourceFactory {
        EmptyPagingSource()
    },
    val showAddPersonItem: Boolean = false,
    val isPendingExpanded: Boolean = true,
    val showInviteCode: String? = null,
    val showInviteButton: Boolean = false,
    val pendingPersons: IPagingSourceFactory<Int, Person> =
        IPagingSourceFactory { EmptyPagingSource() },
    )

class PersonListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val resultReturner: NavResultReturner,
    private val setClipboardStringUseCase: SetClipboardStringUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(PersonListUiState())

    val uiState = _uiState.asStateFlow()

    private val launchDebounced = LaunchDebouncer(viewModelScope)

    private val route: PersonList = savedStateHandle.toRoute()

    private val checkPermissionUseCase: CheckSchoolPermissionsUseCase by inject()

    private val approveOrDeclineInviteRequestUseCase: ApproveOrDeclineInviteRequestUseCase by inject()

    private val pendingPersonsPagingSource = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByPersonStatus = PersonStatusEnum.PENDING_APPROVAL,
            )
        )
    }

    private val pagingSourceFactoryHolder = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listDetailsAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByName = _appUiState.value.searchState.searchText.takeIf { it.isNotBlank() },
                filterByPersonRole = route.filterByRole,
                filterByPersonStatus = PersonStatusEnum.ACTIVE,
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
                expandableFabState = ExpandableFabUiState(
                    visible = !(route.filterByRole != null||route.addToClassUid!=null),
                    items = listOf(
                        ExpandableFabItem(
                            icon = ExpandableFabIcon.INVITE,
                            text =  Res.string.invite_person.asUiText(),
                            onClick = ::onClickInvitePerson,
                        ),
                        ExpandableFabItem(
                            icon = ExpandableFabIcon.ADD,
                            text = Res.string.add_new_person.asUiText(),
                            onClick = ::onClickAdd,
                        )
                    )
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
            val canAddPerson = checkPermissionUseCase(
                PermissionsRequiredByRole.WRITE_PERMISSIONS.flagList
            ).isNotEmpty()

            accountManager.selectedAccountAndPersonFlow.collect { selectedAcct ->
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
            it.copy(
                pendingPersons = pendingPersonsPagingSource,
                persons = pagingSourceFactoryHolder,
                showInviteButton = route.filterByRole != null||route.addToClassUid!=null
            )
        }
    }
    fun onTogglePendingInvites() {
        _uiState.update {
            it.copy(isPendingExpanded = !it.isPendingExpanded)
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


    fun onClickAcceptOrDismissInvite(
        person: Person,
        approved: Boolean,
    ) {
        viewModelScope.launch {
            try {
                approveOrDeclineInviteRequestUseCase(
                    personUid = person.guid,
                    approved = approved,
                )
            }catch(e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PersonEdit.create(
                    null,
                    resultDest = route.resultDest,
                    presetRole = route.filterByRole
                )
            )
        )
    }

    fun onClickInvitePerson() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                InvitePerson.create(
                    invitePersonOptions = InvitePerson.NewUserInviteOptions(null)
                )
            )
        )
    }

}