package world.respect.shared.viewmodel.strand.edit

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import world.respect.shared.navigation.NavCommand
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.navigation.CurriculumDetail
import world.respect.shared.domain.strand.SaveStrandUseCase
import world.respect.shared.domain.strand.GetStrandByIdUseCase
import world.respect.shared.domain.curriculum.models.CurriculumStrand
import world.respect.shared.domain.curriculum.models.SaveStrandParams
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.field_required
import world.respect.shared.generated.resources.save_failed
import world.respect.shared.generated.resources.error_occured
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.load_failed
import world.respect.shared.generated.resources.error_not_found

data class StrandEditUiState(
    val strand: CurriculumStrand? = null,
    val learningObjectives: String = "",
    val outcomes: String = "",
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val nameError: StringResource? = null,
    val learningObjectivesError: StringResource? = null,
    val outcomesError: StringResource? = null,
    val error: StringResource? = null
) {
    val isValid: Boolean
        get() = strand?.name?.isNotBlank() == true &&
                learningObjectives.isNotBlank() &&
                outcomes.isNotBlank() &&
                nameError == null &&
                learningObjectivesError == null &&
                outcomesError == null
}


class StrandEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val saveStrandUseCase: SaveStrandUseCase,
    private val getStrandByIdUseCase: GetStrandByIdUseCase
) : RespectViewModel(savedStateHandle) {

    private var curriculumId: String = ""
    private var strandId: String? = null

    private val _uiState = MutableStateFlow(StrandEditUiState())
    val uiState: StateFlow<StrandEditUiState> = _uiState.asStateFlow()

    override val _navCommandFlow = MutableSharedFlow<NavCommand>()
    override val navCommandFlow = _navCommandFlow.asSharedFlow()

    override val appUiState: StateFlow<AppUiState> = MutableStateFlow(
        AppUiState(
            hideAppBar = true,
            navigationVisible = true
        )
    ).asStateFlow()

    fun setStrandData(curriculumId: String, strandId: String?) {
        this.curriculumId = curriculumId
        this.strandId = strandId
        _uiState.value = _uiState.value.copy(isEditMode = strandId != null)
        val strandIdValue = strandId
        if (strandIdValue != null) {
            loadStrand(strandIdValue)
        } else {
            _uiState.value = _uiState.value.copy(
                strand = CurriculumStrand("", "", "", true)
            )
        }
    }

    fun onNameChange(name: String) {
        val currentStrand = _uiState.value.strand ?: CurriculumStrand("", "", "", true)
        _uiState.value = _uiState.value.copy(
            strand = currentStrand.copy(name = name),
            nameError = null
        )
    }

    fun onLearningObjectivesChange(learningObjectives: String) {
        _uiState.value = _uiState.value.copy(
            learningObjectives = learningObjectives,
            learningObjectivesError = null
        )
    }

    fun onOutcomesChange(outcomes: String) {
        _uiState.value = _uiState.value.copy(
            outcomes = outcomes,
            outcomesError = null
        )
    }

    fun onBackClick() {
        viewModelScope.launch {
            _navCommandFlow.emit(
                NavCommand.Navigate(
                    CurriculumDetail(
                        curriculumId = curriculumId,
                        curriculumName = ""
                    )
                )
            )
        }
    }

    fun onSaveClick() {
        if (validateForm()) {
            saveStrand()
        }
    }

    private fun validateForm(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        val nameError = if (currentState.strand?.name?.isBlank() != false) {
            isValid = false
            Res.string.field_required
        } else {
            null
        }

        val learningObjectivesError = if (currentState.learningObjectives.isBlank()) {
            isValid = false
            Res.string.field_required
        } else {
            null
        }

        val outcomesError = if (currentState.outcomes.isBlank()) {
            isValid = false
            Res.string.field_required
        } else {
            null
        }

        _uiState.value = currentState.copy(
            nameError = nameError,
            learningObjectivesError = learningObjectivesError,
            outcomesError = outcomesError
        )

        return isValid
    }

    private fun saveStrand() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val currentState = _uiState.value
                val params = SaveStrandParams(
                    curriculumId = curriculumId,
                    strandId = strandId,
                    name = currentState.strand?.name ?: "",
                    learningObjectives = currentState.learningObjectives,
                    outcomes = currentState.outcomes
                )

                val result = saveStrandUseCase(params)

                result.fold(
                    onSuccess = { savedStrand ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _navCommandFlow.emit(
                            NavCommand.Navigate(
                                CurriculumDetail(
                                    curriculumId = curriculumId,
                                    curriculumName = ""
                                )
                            )
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = Res.string.save_failed
                        )
                    }
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = Res.string.error_occured
                )
            }
        }
    }

    private fun loadStrand(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val curriculumStrand = getStrandByIdUseCase(id)

                if (curriculumStrand != null) {
                    val parts = curriculumStrand.description.split("\n\nExpected Outcomes: ")
                    val learningObjectives = parts[0].removePrefix("Learning Objectives: ")
                    val outcomes = if (parts.size > 1) parts[1] else ""

                    _uiState.value = _uiState.value.copy(
                        strand = curriculumStrand,
                        learningObjectives = learningObjectives,
                        outcomes = outcomes,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = Res.string.error_not_found
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = Res.string.load_failed
                )
            }
        }
    }
}