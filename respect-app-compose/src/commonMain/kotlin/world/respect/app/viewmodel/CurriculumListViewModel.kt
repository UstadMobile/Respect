// File: app/src/main/java/world/respect/app/viewmodel/CurriculumListViewModel.kt
package world.respect.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.CurriculumEdit
import world.respect.shared.navigation.CurriculumDetail
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.app.domain.models.Curriculum
import world.respect.app.domain.usecase.curriculum.GetCurriculaUseCase
import world.respect.shared.navigation.RespectAppRoute

data class CurriculumListUiState(
    val curricula: List<Curriculum> = emptyList(),
    val selectedTab: Int = TabConstants.BY_CURRICULUM,
    val isLoading: Boolean = false,
    val error: String? = null
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
                        val errorMessage = exception.message ?: UNKNOWN_ERROR_MESSAGE
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                    .collect { curricula ->
                        _uiState.value = _uiState.value.copy(
                            curricula = curricula,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                val errorMessage = e.message ?: UNKNOWN_ERROR_MESSAGE
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    companion object {
        private const val UNKNOWN_ERROR_MESSAGE = "Unknown error occurred"
    }
}