package world.respect.shared.viewmodel.clazz.addperson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.generated.resources.add_student
import world.respect.shared.generated.resources.add_teacher
import world.respect.shared.navigation.AddPersonToClazz
import world.respect.shared.util.ext.asUiText


data class AddPersonToClazzUIState(
    val name: String = "",
    val inviteCode: String? = null,
)

class AddPersonToClazzViewModel(
    savedStateHandle: SavedStateHandle,
    private val setClipboardStringUseCase: SetClipboardStringUseCase,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(AddPersonToClazzUIState())

    val uiState = _uiState.asStateFlow()

    private val route: AddPersonToClazz = savedStateHandle.toRoute()


    init {
        _uiState.update { it.copy(inviteCode = route.inviteCode) }

        viewModelScope.launch {
            _appUiState.update {
                if (route.roleType == EnrollmentRoleEnum.STUDENT) {
                    it.copy(
                        title = Res.string.add_student.asUiText()
                    )
                } else {
                    it.copy(
                        title = Res.string.add_teacher.asUiText()
                    )
                }
            }
        }
    }

    fun onClickInviteCode() {
        setClipboardStringUseCase(_uiState.value.inviteCode ?: "")
    }
}