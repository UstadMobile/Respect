package world.respect.shared.viewmodel.schooldirectory.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.clazz
import world.respect.shared.generated.resources.school_directory
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue

data class SchoolDirectoryListUIState(
    val schoolDirectory: List<SchoolDirectoryEntry> = emptyList(),
)

class SchoolDirectoryListViewModel(
    private val accountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()
    private val respectAppDataSource: RespectAppDataSource by inject()
    private val _uiState = MutableStateFlow(SchoolDirectoryListUIState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.school_directory.asUiText(),
                hideBottomNavigation = true,
                fabState = it.fabState.copy(
                    icon = FabUiState.FabIcon.ADD,
                    text = Res.string.school_directory.asUiText(),
                    onClick = ::onClickAdd,
                    visible = true
                )
            )
        }
        loadSchoolDirectories()
    }

    private fun loadSchoolDirectories() {
        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryEntryDataSource.listAsFlow(
                loadParams = DataLoadParams(),
                listParams = SchoolDirectoryEntryDataSource.GetListParams()
            ).collect { dataState ->
                if (dataState is DataReadyState) {
                    _uiState.update { prev ->
                        prev.copy(schoolDirectory = dataState.data)
                    }
                }
            }

        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SchoolDirectoryEdit)
        )
    }

    fun onDeleteDirectory(directory: SchoolDirectoryEntry) {
        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryEntryDataSource.deleteDirectory(directory)
            loadSchoolDirectories()
        }
    }
}
