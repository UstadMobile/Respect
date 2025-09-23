package world.respect.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.lets_get_started
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOption
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


class GetStartedViewModel(
    savedStateHandle: SavedStateHandle,
    val respectAppDataSource: RespectAppDataSource
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(GetStartedUiState())
    val uiState = _uiState.asStateFlow()
    private val debouncer = LaunchDebouncer(viewModelScope)

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.lets_get_started.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = false,
            )
        }
    }

    fun onSchoolNameChanged(name: String) {
        _uiState.update { it.copy(schoolName = name) }

        if (name.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList(), errorMessage = null, showButtons = true) }
            return
        }

        debouncer.launch(RESPECT_REALMS) {
            respectAppDataSource.schoolDirectoryEntryDataSource.listAsFlow(
                loadParams = DataLoadParams(),
                listParams = SchoolDirectoryEntryDataSource.GetListParams(
                    name = name
                )
            ).collect { dataState ->
                if(dataState is DataReadyState) {
                    _uiState.update {
                        it.copy(
                            suggestions = dataState.data
                        )
                    }
                }
            }
        }
    }


    fun onSchoolSelected(school: SchoolDirectoryEntry) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LoginScreen.create(school.self)
            )
        )
    }

    fun onClickOtherOptions() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(OtherOption))
    }

    companion object {

        const val RESPECT_REALMS = "respectRealms"

    }
}

data class GetStartedUiState(
    val schoolName: String = "",
    val errorText: String? = null,
    val showButtons: Boolean = true,
    val errorMessage: UiText? = null,
    val suggestions: List<SchoolDirectoryEntry> = emptyList()

)
