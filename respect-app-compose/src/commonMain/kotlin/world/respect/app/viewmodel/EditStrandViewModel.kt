package world.respect.app.viewmodel

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
import world.respect.app.domain.usecase.strand.SaveStrandUseCase
import world.respect.app.domain.usecase.strand.GetStrandByIdUseCase

class StrandEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val saveStrandUseCase: SaveStrandUseCase,
    private val getStrandByIdUseCase: GetStrandByIdUseCase
) : RespectViewModel(savedStateHandle) {

    private var curriculumId: String = ""
    private var strandId: String? = null

    data class StrandEditUiState(
        val strand: Strand? = null,
        val name: String = "",
        val learningObjectives: String = "",
        val outcomes: String = "",
        val isLoading: Boolean = false,
        val isEditMode: Boolean = false,
        val nameError: String? = null,
        val learningObjectivesError: String? = null,
        val outcomesError: String? = null,
        val error: String? = null
    ) {
        val isValid: Boolean
            get() = name.isNotBlank() && learningObjectives.isNotBlank() &&
                    outcomes.isNotBlank() && nameError == null &&
                    learningObjectivesError == null && outcomesError == null
    }

    data class Strand(
        val id: String,
        val name: String,
        val learningObjectives: String,
        val outcomes: String,
        val isActive: Boolean
    )

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
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
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

        val nameError = if (currentState.name.isBlank()) {
            isValid = false
            VALIDATION_NAME_REQUIRED
        } else {
            null
        }

        val learningObjectivesError = if (currentState.learningObjectives.isBlank()) {
            isValid = false
            VALIDATION_LEARNING_OBJECTIVES_REQUIRED
        } else {
            null
        }

        val outcomesError = if (currentState.outcomes.isBlank()) {
            isValid = false
            VALIDATION_OUTCOMES_REQUIRED
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
                val params = SaveStrandUseCase.SaveStrandParams(
                    curriculumId = curriculumId,
                    strandId = strandId,
                    name = currentState.name,
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
                        val errorMessage = exception.message ?: SAVE_FAILED_ERROR
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                )

            } catch (e: Exception) {
                val errorMessage = e.message ?: UNKNOWN_ERROR_MESSAGE
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
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

                    val strand = Strand(
                        id = curriculumStrand.id,
                        name = curriculumStrand.name,
                        learningObjectives = learningObjectives,
                        outcomes = outcomes,
                        isActive = curriculumStrand.isActive
                    )

                    _uiState.value = _uiState.value.copy(
                        strand = strand,
                        name = strand.name,
                        learningObjectives = strand.learningObjectives,
                        outcomes = strand.outcomes,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = STRAND_NOT_FOUND_ERROR
                    )
                }

            } catch (e: Exception) {
                val errorMessage = e.message ?: LOAD_FAILED_ERROR
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    companion object {
        private const val VALIDATION_NAME_REQUIRED = "name_required"
        private const val VALIDATION_LEARNING_OBJECTIVES_REQUIRED = "learning_objectives_required"
        private const val VALIDATION_OUTCOMES_REQUIRED = "outcomes_required"
        private const val SAVE_FAILED_ERROR = "Failed to save strand"
        private const val LOAD_FAILED_ERROR = "Failed to load strand"
        private const val STRAND_NOT_FOUND_ERROR = "Strand not found"
        private const val UNKNOWN_ERROR_MESSAGE = "Unknown error occurred"
    }
}