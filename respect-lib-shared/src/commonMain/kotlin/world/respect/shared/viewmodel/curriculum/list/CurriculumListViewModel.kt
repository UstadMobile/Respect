
package world.respect.shared.viewmodel.curriculum.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.CurriculumEdit
import world.respect.shared.navigation.CurriculumDetail
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.domain.curriculum.models.Curriculum
import world.respect.shared.domain.curriculum.GetCurriculaUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_occured
import world.respect.shared.navigation.RespectAppRoute

data class CurriculumListUiState(
    val curricula: List<Curriculum> = emptyList(),
    val selectedTab: Int = TabConstants.BY_CURRICULUM,
    val isLoading: Boolean = false,
    val error: StringResource? = null
)

object TabConstants {
    const val FOR_YOU = 0
    const val BY_CURRICULUM = 1
}

class CurriculumListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCurriculaUseCase: GetCurriculaUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(CurriculumListUiState())
    val uiState: StateFlow<CurriculumListUiState> = _uiState.asStateFlow()

    override val _navCommandFlow = MutableSharedFlow<NavCommand>()
    override val navCommandFlow = _navCommandFlow.asSharedFlow()

    override val appUiState: StateFlow<AppUiState> = MutableStateFlow(
        AppUiState(
            hideAppBar = true,
            navigationVisible = true
        )
    ).asStateFlow()

    init {
        loadCurricula()
    }

    fun onTabSelected(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onAddCurriculumClick() {
        viewModelScope.launch {
            _navCommandFlow.emit(NavCommand.Navigate(CurriculumEdit()))
        }
    }

    fun onProfileClick() {
        // TODO: Implement profile navigation
    }

    fun onBottomNavClick(destination: Any) {
        viewModelScope.launch {
            _navCommandFlow.emit(NavCommand.Navigate(destination as RespectAppRoute))
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _navCommandFlow.emit(NavCommand.Navigate(world.respect.shared.navigation.RespectAppLauncher))
        }
    }

    fun onCurriculumClick(curriculum: Curriculum) {
        viewModelScope.launch {
            _navCommandFlow.emit(
                NavCommand.Navigate(
                    CurriculumDetail(
                        curriculumId = curriculum.id,
                        curriculumName = curriculum.name
                    )
                )
            )
        }
    }

    fun onRefresh() {
        loadCurricula()
    }

    private fun loadCurricula() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                getCurriculaUseCase()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = Res.string.error_occured
                        )
                    }
                    .collect { curricula ->
                        _uiState.value = _uiState.value.copy(
                            curricula = curricula,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = Res.string.error_occured
                )
            }
        }
    }
}