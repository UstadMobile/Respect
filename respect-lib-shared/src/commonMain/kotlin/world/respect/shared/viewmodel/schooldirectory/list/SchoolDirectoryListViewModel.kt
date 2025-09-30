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
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.school_directory
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryEdit
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue

data class SchoolDirectoryListUIState(
    val schoolDirectory: List<RespectSchoolDirectory> = emptyList(),
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
            val data = respectAppDataSource.schoolDirectoryDataSource.allDirectories()

            _uiState.update { prev ->
                prev.copy(schoolDirectory = data)
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = SchoolDirectoryEdit,
                popUpTo = SchoolDirectoryList
            )
        )
    }

    fun onDeleteDirectory(directory: RespectSchoolDirectory) {
        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryDataSource.deleteDirectory(directory)
            loadSchoolDirectories()
        }
    }
}
