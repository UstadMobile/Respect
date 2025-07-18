
package world.respect.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import androidx.lifecycle.viewModelScope
import world.respect.shared.navigation.EditStrand
import world.respect.shared.navigation.CurriculumList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.app.domain.usecase.strand.GetStrandsByCurriculumIdUseCase

data class CurriculumDetailUiState(
    val curriculumId: String = "",
    val curriculumName: String = "",
    val strands: List<CurriculumStrand> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
data class CurriculumStrand(
    val id: String,
    val name: String,
    val description: String = "",
    val isActive: Boolean = true
)
class CurriculumDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getStrandsByCurriculumIdUseCase: GetStrandsByCurriculumIdUseCase
) : RespectViewModel(savedStateHandle) {

    private var curriculumId: String = ""
    private var curriculumName: String = ""

    private val _uiState = MutableStateFlow(CurriculumDetailUiState())
    val uiState: StateFlow<CurriculumDetailUiState> = _uiState.asStateFlow()

    override val _navCommandFlow = MutableSharedFlow<NavCommand>()
    override val navCommandFlow = _navCommandFlow.asSharedFlow()

    override val appUiState: StateFlow<AppUiState> = MutableStateFlow(
        AppUiState(
            hideAppBar = true,
            navigationVisible = true
        )
    ).asStateFlow()

    fun setCurriculumData(curriculumId: String, curriculumName: String) {
        this.curriculumId = curriculumId
        this.curriculumName = curriculumName
        _uiState.value = _uiState.value.copy(
            curriculumId = curriculumId,
            curriculumName = curriculumName
        )
        loadCurriculumDetails()
    }
    fun onBackClick() {
        viewModelScope.launch {
            _navCommandFlow.emit(NavCommand.Navigate(CurriculumList))
        }
    }

    fun onAddStrandClick() {
        viewModelScope.launch {
            _navCommandFlow.emit(
                NavCommand.Navigate(
                    EditStrand(
                        curriculumId = curriculumId,
                        strandId = null
                    )
                )
            )
        }
    }
    fun onStrandClick(strand: CurriculumStrand) {
        viewModelScope.launch {
            _navCommandFlow.emit(
                NavCommand.Navigate(
                    EditStrand(
                        curriculumId = curriculumId,
                        strandId = strand.id
                    )
                )
            )
        }
    }

    fun onRefresh() {
        loadCurriculumDetails()
    }

    private fun loadCurriculumDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                getStrandsByCurriculumIdUseCase(curriculumId)
                    .catch { exception ->
                        val errorMessage = exception.message ?: UNKNOWN_ERROR_MESSAGE
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                    .collect { strands ->
                        _uiState.value = _uiState.value.copy(
                            strands = strands,
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