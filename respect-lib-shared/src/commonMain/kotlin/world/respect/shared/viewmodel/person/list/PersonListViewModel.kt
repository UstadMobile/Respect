package world.respect.shared.viewmodel.person.list

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.shared.paging.EmptyPagingSource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.people
import world.respect.shared.generated.resources.person
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import io.github.aakira.napier.Napier


data class PersonListUiState(
    val persons: () -> PagingSource<Int, PersonListDetails> = { EmptyPagingSource() },
)

class PersonListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(PersonListUiState())

    val uiState = _uiState.asStateFlow()

    private val pagingSourceFactory: () -> PagingSource<Int, PersonListDetails> = {
        Napier.d("PersonListViewModel: pagingSourceFactory invoke")
        schoolDataSource.personDataSource.findAllListDetailsAsPagingSource(
            DataLoadParams(), null
        )
    }

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.people.asUiText(),
                fabState = FabUiState(
                    visible = true,
                    onClick = ::onClickAdd,
                    text = Res.string.person.asUiText(),
                    icon = FabUiState.FabIcon.ADD,
                )
            )
        }

        _uiState.update {
            it.copy(
                persons = pagingSourceFactory
            )
        }
    }

    fun onClickItem(person: PersonListDetails) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PersonDetail(person.guid))
        )
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PersonEdit(null))
        )
    }

}