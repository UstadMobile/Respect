package world.respect.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.shared.domain.getwarnings.GetWarningsUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.lets_get_started
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOption
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class GetStartedUiState(
    val schoolName: String = "",
    val errorText: String? = null,
    val showButtons: Boolean = true,
    val errorMessage: UiText? = null,
    val suggestions: List<SchoolDirectoryEntry> = emptyList(),
    val warning: UiText? = null,
)


class GetStartedViewModel(
    savedStateHandle: SavedStateHandle,
    val respectAppDataSource: RespectAppDataSource,
    private val getWarningsUseCase: GetWarningsUseCase? = null,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(GetStartedUiState())
    val uiState = _uiState.asStateFlow()
    private val debouncer = LaunchDebouncer(viewModelScope)

    private val route: GetStartedScreen = savedStateHandle.toRoute()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.lets_get_started.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = route.canGoBack,
            )
        }

        viewModelScope.launch {
            val warning = getWarningsUseCase?.invoke()
            _uiState.takeIf { warning != null }?.update { it.copy(warning = warning) }
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
