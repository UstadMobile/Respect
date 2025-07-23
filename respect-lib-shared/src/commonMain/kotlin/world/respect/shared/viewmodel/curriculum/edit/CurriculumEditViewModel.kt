package world.respect.shared.viewmodel.curriculum.edit

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.domain.curriculum.models.Curriculum
import androidx.lifecycle.viewModelScope
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.navigation.NavCommand
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.navigation.CurriculumDetail
import world.respect.shared.navigation.CurriculumList
import world.respect.shared.domain.curriculum.GetCurriculumByIdUseCase
import world.respect.shared.domain.curriculum.SaveCurriculumUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_not_found
import world.respect.shared.generated.resources.error_occured
import world.respect.shared.generated.resources.field_required
import world.respect.shared.generated.resources.load_failed
import world.respect.shared.generated.resources.save_failed

data class CurriculumEditUiState(
    val curriculum: Curriculum? = null,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val nameError: StringResource? = null,
    val idError: StringResource? = null,
    val descriptionError: StringResource? = null,
    val error: StringResource? = null
) {
    val isValid: Boolean
        get() = curriculum?.name?.isNotBlank() == true &&
                curriculum?.id?.isNotBlank() == true &&
                nameError == null &&
                idError == null &&
                descriptionError == null
}


class CurriculumEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCurriculumByIdUseCase: GetCurriculumByIdUseCase,
    private val saveCurriculumUseCase: SaveCurriculumUseCase
) : RespectViewModel(savedStateHandle) {

    private val curriculumId: String? = savedStateHandle.get<String>("curriculumId")

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
        } else {

            _uiState.value = _uiState.value.copy(
                curriculum = Curriculum("", "", "", true)
            )
        }
    }

    fun onNameChange(name: String) {
        val currentCurriculum = _uiState.value.curriculum ?: Curriculum("", "", "", true)
        _uiState.value = _uiState.value.copy(
            curriculum = currentCurriculum.copy(name = name),
            nameError = null
        )
    }

    fun onIdChange(id: String) {
        val currentCurriculum = _uiState.value.curriculum ?: Curriculum("", "", "", true)
        _uiState.value = _uiState.value.copy(
            curriculum = currentCurriculum.copy(id = id),
            idError = null
        )
    }

    fun onDescriptionChange(description: String) {
        val currentCurriculum = _uiState.value.curriculum ?: Curriculum("", "", "", true)
        _uiState.value = _uiState.value.copy(
            curriculum = currentCurriculum.copy(description = description),
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

        val nameError = if (currentState.curriculum?.name?.isBlank() != false) {
            isValid = false
            Res.string.field_required
        } else {
            null
        }

        val idError = if (currentState.curriculum?.id?.isBlank() != false) {
            isValid = false
            Res.string.field_required
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
                val curriculum = _uiState.value.curriculum ?: return@launch

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

    private fun loadCurriculum(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val curriculum = getCurriculumByIdUseCase(id)

                if (curriculum != null) {
                    _uiState.value = _uiState.value.copy(
                        curriculum = curriculum,
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