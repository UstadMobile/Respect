package world.respect.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.lets_get_started
import world.respect.shared.generated.resources.school_not_exist_error
import world.respect.shared.navigation.JoinClazzWithCode
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOption
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel


class GetStartedViewModel(
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(GetStartedUiState())
    val uiState = _uiState.asStateFlow()

    private val schoolList = listOf(
        School("respect school", "https://testproxy.devserver3.ustadmobile.com/"),
        School("respect 2 school", "https://respect2.com"),
        School("spix school", "https://spix.com"),
        School("ustad school", "https://ustad.com"),
    )
    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = getString(Res.string.lets_get_started),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                    showBackButton = false
                )
            }
        }
    }

    fun onSchoolNameChanged(name: String) {
        val suggestions = if (name.isBlank()) {
            emptyList()
        } else {
            schoolList.filter { it.name.contains(name, ignoreCase = true) }
        }

        _uiState.update {
            it.copy(
                schoolName = name,
                errorMessage = if (suggestions.isEmpty())
                    StringResourceUiText(Res.string.school_not_exist_error) else null,
                suggestions = suggestions,
                showButtons = suggestions.isEmpty()
            )
        }
    }

    fun onClickIHaveCode() {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(JoinClazzWithCode)
            )
    }

    fun onSchoolSelected(school: School) {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(LoginScreen)
            )
    }
    fun onClickOtherOptions() {
            _navCommandFlow.tryEmit(NavCommand.Navigate(OtherOption))
    }

}
data class School(
    val name: String,
    val url: String
)
data class GetStartedUiState(
    val schoolName: String = "",
    val errorText: String? = null,
    val showButtons: Boolean = true,
    val errorMessage: StringResourceUiText? = null,
    val suggestions: List<School> = emptyList()

)
