package world.respect.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.settings
import world.respect.shared.navigation.CurriculumMappingList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.Settings
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class UiLanguage(val langCode: String, val langDisplay: String)

data class SettingsUiState(
    val loading: Boolean = false,
    val langDialogVisible: Boolean = false,
    val currentLanguage: String = "",
    val availableLanguages: List<UiLanguage> = emptyList()

)

class SettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.settings.asUiText(),
                navigationVisible = true,
                hideAppBar = false,
                userAccountIconVisible = true,
                hideBottomNavigation = true,
            )
        }
        _uiState.update {
            it.copy(
                availableLanguages = listOf(
                    UiLanguage("en", "English"),
                    UiLanguage("hi", "Hindi"),
                    UiLanguage("fr", "French")
                ),
                currentLanguage = "English"
            )
        }
    }

    fun onClickSettings() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(Settings)
        )
    }
    fun onNavigateToLanguage() {
        // TODO
    }

    fun onClickLanguage() {
        _uiState.update { prev ->
            prev.copy(
                langDialogVisible = true
            )
        }
    }

    fun onDismissLangDialog() {
        _uiState.update { prev ->
            prev.copy(langDialogVisible = false)
        }
    }

    fun onClickLang(lang: UiLanguage) {

        _uiState.update { prev ->
            prev.copy(langDialogVisible = false)
        }

        _uiState.update { prev ->
            prev.copy(
                currentLanguage = lang.langDisplay
            )
        }


    }

    fun onNavigateToMapping() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CurriculumMappingList)
        )
    }
}
