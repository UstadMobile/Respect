package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.teacher_admin_login
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class TeacherAndAdminLoginUiState(
    val errorMessage: UiText? = null,
    val pin: String = "",
)

class TeacherAndAdminLoginViewmodel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val respectAppDataSource: RespectAppDataSource
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(TeacherAndAdminLoginUiState())

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update {
            it.copy(
                title = Res.string.teacher_admin_login.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onPinChanged(pin: String) {
        _uiState.update { it.copy(pin = pin) }
    }

    fun onClickNext() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(GetStartedScreen())
        )
    }

    suspend fun verifyTeacherPin(enteredPin: String): Boolean {
        val activeAccount = accountManager.activeAccount ?: return false
        val schoolEntry =respectAppDataSource.schoolDirectoryEntryDataSource.getSchoolDirectoryEntryByUrl(
            activeAccount.school.self
        ).dataOrNull() ?: return false

        return schoolEntry.teacherPin == enteredPin
    }
}