
package world.respect.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.app.domain.models.Curriculum
import androidx.lifecycle.viewModelScope
import world.respect.shared.navigation.NavCommand
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.navigation.CurriculumDetail
import world.respect.shared.navigation.CurriculumList
import world.respect.app.domain.usecase.curriculum.GetCurriculumByIdUseCase
import world.respect.app.domain.usecase.curriculum.SaveCurriculumUseCase

class CurriculumEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCurriculumByIdUseCase: GetCurriculumByIdUseCase,
    private val saveCurriculumUseCase: SaveCurriculumUseCase
) : RespectViewModel(savedStateHandle) {

    private val curriculumId: String? = savedStateHandle.get<String>("curriculumId")

    data class CurriculumEditUiState(
        val curriculum: Curriculum? = null,
        val name: String = "",
        val id: String = "",
        val description: String = "",
        val isLoading: Boolean = false,
        val isEditMode: Boolean = false,
        val nameError: String? = null,
        val idError: String? = null,
        val descriptionError: String? = null,
        val error: String? = null
    ) {
        val isValid: Boolean
            get() = name.isNotBlank() &&
                    id.isNotBlank() &&
                    nameError == null &&
                    idError == null &&
                    descriptionError == null
    }

    private val _uiState = MutableStateFlow(
        CurriculumEditUiState(isEditMode = curriculumId != null)
    )
    val uiState: StateFlow<CurriculumEditUiState> = _uiState.asStateFlow()

    override val _navCommandFlow = MutableSharedFlow<NavCommand>()
    override val navCommandFlow = _navCommandFlow.asSharedFlow()

    override val appUiState: StateFlow<AppUiState> = MutableStateFlow(
        AppUiState(
            hideAppBar = true,
            navigationVisible = true
        )
    ).asStateFlow()

    init {
        val curriculumIdValue = curriculumId
        if (curriculumIdValue != null) {
            loadCurriculum(curriculumIdValue)
        }
    }
    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }
    fun onIdChange(id: String) {
        _uiState.value = _uiState.value.copy(
            id = id,
            idError = null
        )
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            descriptionError = null
        )
    }

    fun onBackClick() {
        viewModelScope.launch {
            _navCommandFlow.emit(NavCommand.Navigate(CurriculumList))
        }
    }

    fun onSaveClick() {
        if (validateForm()) {
            saveCurriculum()
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

        val idError = if (currentState.id.isBlank()) {
            isValid = false
            VALIDATION_ID_REQUIRED
        } else {
            null
        }

        _uiState.value = currentState.copy(
            nameError = nameError,
            idError = idError,
            descriptionError = null
        )

        return isValid
    }
    private fun saveCurriculum() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val currentState = _uiState.value
                val curriculum = Curriculum(
                    id = currentState.id,
                    name = currentState.name,
                    description = currentState.description,
                    isActive = true
                )

                val result = saveCurriculumUseCase(curriculum)

                result.fold(
                    onSuccess = { savedCurriculum ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _navCommandFlow.emit(
                            NavCommand.Navigate(
                                CurriculumDetail(
                                    curriculumId = savedCurriculum.id,
                                    curriculumName = savedCurriculum.name
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
    private fun loadCurriculum(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val curriculum = getCurriculumByIdUseCase(id)

                if (curriculum != null) {
                    _uiState.value = _uiState.value.copy(
                        curriculum = curriculum,
                        name = curriculum.name,
                        id = curriculum.id,
                        description = curriculum.description,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = CURRICULUM_NOT_FOUND_ERROR
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
        private const val VALIDATION_ID_REQUIRED = "id_required"
        private const val SAVE_FAILED_ERROR = "Failed to save curriculum"
        private const val LOAD_FAILED_ERROR = "Failed to load curriculum"
        private const val CURRICULUM_NOT_FOUND_ERROR = "Curriculum not found"
        private const val UNKNOWN_ERROR_MESSAGE = "Unknown error occurred"
    }
}