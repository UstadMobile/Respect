package world.respect.shared.viewmodel.settings

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig
import world.respect.shared.domain.applanguage.RespectMobileSystemCommon
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.applanguage.SetLanguageUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.settings
import world.respect.shared.navigation.CurriculumMappingList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class SettingsUiState(
    val loading: Boolean = false,
    val langDialogVisible: Boolean = false,
    val currentLanguage: String = "",
    val availableLanguages: List<RespectMobileSystemCommon.UiLanguage> = emptyList(),
    val waitForRestartDialogVisible: Boolean = false,

    )

class SettingsViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    supportedLangConfig: SupportedLanguagesConfig,
    systemImpl: RespectMobileSystemCommon,
    private val setLanguageUseCase: SetLanguageUseCase,
    ) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()
    private val availableLangs = supportedLangConfig.supportedUiLanguagesAndSysDefault(systemImpl)



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


            val savedLangCode = supportedLangConfig.localeSetting ?: RespectMobileSystemCommon.LOCALE_USE_SYSTEM

            val currentLang = if (savedLangCode == RespectMobileSystemCommon.LOCALE_USE_SYSTEM) {
                val resolvedLocale = supportedLangConfig.selectFirstSupportedLocale().langCode
                availableLangs.firstOrNull { it.langCode == resolvedLocale }
            } else {
                availableLangs.firstOrNull { it.langCode == savedLangCode }
            } ?: availableLangs.first()

            _uiState.update { it.copy(
                availableLanguages = availableLangs,
                currentLanguage = currentLang.langDisplay
            )}

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

    fun onClickLang(lang: RespectMobileSystemCommon.UiLanguage) {
        _uiState.update { prev ->
            prev.copy(langDialogVisible = false)
        }

        val result = setLanguageUseCase(
            uiLang = lang,
        )

        println("Result language $result $lang")
        if(result.waitForRestart) {
            _uiState.update { prev ->
                prev.copy(
                    waitForRestartDialogVisible = true,
                    currentLanguage = lang.langDisplay
                )
            }
        }else {
            _uiState.update { prev ->
                prev.copy(
                    currentLanguage = lang.langDisplay
                )
            }
        }

    }

    fun onNavigateToMapping() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CurriculumMappingList)
        )
    }

}
