package world.respect.shared.viewmodel.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.respect.shared.viewmodel.RespectViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.applanguage.GetUiLanguagesUseCase
import world.respect.shared.domain.applanguage.SetLanguageUseCase
import world.respect.shared.domain.applanguage.SupportedLanguagesConfig
import world.respect.shared.domain.navigation.onappstart.NavigateOnAppStartUseCase
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.domain.usagereporting.GetUsageReportingEnabledUseCase
import world.respect.shared.domain.usagereporting.SetUsageReportingEnabledUseCase

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val usageStatsOptInChecked: Boolean = true,
    val availableLanguages: List<SupportedLanguagesConfig.UiLanguage> = emptyList(),
    val selectedLanguage: SupportedLanguagesConfig.UiLanguage? = null
)

class OnboardingViewModel(
    savedStateHandle: SavedStateHandle,
    private val settings: Settings,
    private val setUsageReportingEnabledUseCase: SetUsageReportingEnabledUseCase,
    private val getUsageReportingEnabledUseCase: GetUsageReportingEnabledUseCase,
    private val navigateOnAppStartUseCase: NavigateOnAppStartUseCase,
    private val getUiLanguagesUseCase: GetUiLanguagesUseCase,
    private val setLanguageUseCase: SetLanguageUseCase
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                hideAppBar = true
            )
        }
        val uiLanguages = getUiLanguagesUseCase()
        _uiState.update {
            it.copy(
                usageStatsOptInChecked = getUsageReportingEnabledUseCase(),
                availableLanguages = uiLanguages.availableLanguages,
                selectedLanguage = uiLanguages.selectedLanguage,
            )
        }
    }

    fun onToggleUsageStatsOptIn() {
        _uiState.update { prev ->
            prev.copy(
                usageStatsOptInChecked = !prev.usageStatsOptInChecked
            )
        }
    }

    fun onClickGetStartedButton() {

        settings.putString(ShouldShowOnboardingUseCase.KEY_ONBOARDING_SHOWN, true.toString())
        setUsageReportingEnabledUseCase(_uiState.value.usageStatsOptInChecked)
        viewModelScope.launch {
            _navCommandFlow.tryEmit(navigateOnAppStartUseCase())
        }

    }

    fun onLanguageSelected(lang: SupportedLanguagesConfig.UiLanguage) {
        setLanguageUseCase(uiLang = lang)
        _uiState.update { it.copy(selectedLanguage = lang) }
    }
}