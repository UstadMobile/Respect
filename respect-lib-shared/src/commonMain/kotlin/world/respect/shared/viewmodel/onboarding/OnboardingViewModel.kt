package world.respect.shared.viewmodel.onboarding

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.viewmodel.RespectViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.update
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.domain.usagereporting.GetUsageReportingEnabledUseCase
import world.respect.shared.domain.usagereporting.SetUsageReportingEnabledUseCase
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val usageStatsOptInChecked: Boolean = true,
)

class OnboardingViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val settings: Settings,
    private val setUsageReportingEnabledUseCase: SetUsageReportingEnabledUseCase,
    private val getUsageReportingEnabledUseCase: GetUsageReportingEnabledUseCase,
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
        _uiState.update { it.copy(usageStatsOptInChecked = getUsageReportingEnabledUseCase()) }
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

        val hasAccount = accountManager.selectedAccount != null

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = GetStartedScreen(),
                clearBackStack = hasAccount,
            )
        )
    }

}