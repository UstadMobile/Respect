package world.respect.shared.viewmodel.curriculum.detail

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import androidx.lifecycle.viewModelScope
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.navigation.EditStrand
import world.respect.shared.navigation.CurriculumList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.domain.strand.GetStrandsByCurriculumIdUseCase
import world.respect.shared.domain.curriculum.models.CurriculumStrand
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_occured

data class CurriculumDetailUiState(
    val curriculumId: String = "",
    val curriculumName: String = "",
    val strands: List<CurriculumStrand> = emptyList(),
    val isLoading: Boolean = false,
    val error: StringResource? = null
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
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = Res.string.error_occured
                        )
                    }
                    .collect { strands ->
                        _uiState.value = _uiState.value.copy(
                            strands = strands,
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