package world.respect.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.applanguage.SetLanguageUseCase
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig.Companion.LOCALE_USE_SYSTEM
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.settings
import world.respect.shared.navigation.CurriculumMappingList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class SettingsUiState(
    val loading: Boolean = false,
    val langDialogVisible: Boolean = false,
    val currentLanguage: SupportedLanguagesConfig.UiLanguage? = null,

    val availableLanguages: List<SupportedLanguagesConfig.UiLanguage> = emptyList(),
    val waitForRestartDialogVisible: Boolean = false
)

class SettingsViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val supportedLangConfig: SupportedLanguagesConfig,
    private val setLanguageUseCase: SetLanguageUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

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

        loadLanguages()
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

    fun onClickLang(lang: SupportedLanguagesConfig.UiLanguage) {
        _uiState.update { prev ->
            prev.copy(langDialogVisible = false)
        }

        setLanguageUseCase(uiLang = lang)

        loadLanguages()
    }

    fun loadLanguages() {
        viewModelScope.launch {

            val availableLangs = supportedLangConfig.getAvailableLanguages()

            val langSetting = supportedLangConfig.localeSetting ?: LOCALE_USE_SYSTEM

            val currentLang = availableLangs.firstOrNull {
                it.langCode == langSetting
            } ?: availableLangs.first()

            _uiState.update {
                it.copy(
                    availableLanguages = availableLangs,
                    currentLanguage = currentLang

                )
            }
        }
    }
}
