package world.respect.shared.viewmodel.acknowledgement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.getplaystorereferrer.GetDeferredDeepLinkUseCase
import world.respect.shared.domain.navigation.deeplink.CustomDeepLinkToUrlUseCase
import world.respect.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCase
import world.respect.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import world.respect.shared.ext.withClearBackstack
import world.respect.shared.navigation.Acknowledgement
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.Onboarding
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.viewmodel.RespectViewModel

data class AcknowledgementUiState(
    val isLoading: Boolean = false,
)
class AcknowledgementViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
    private val initDeepLinkUriProvider: InitDeepLinkUriProviderUseCase,
    private val customDeepLinkToUrlUseCase: CustomDeepLinkToUrlUseCase,
    private val resolveUrlToNavCommandUseCase: ResolveUrlToNavCommandUseCase,
    private val settings: Settings,
) : RespectViewModel(savedStateHandle), KoinComponent {

    private val _uiState = MutableStateFlow(AcknowledgementUiState())

    private val getDeferredDeepLinkUseCase: GetDeferredDeepLinkUseCase? by lazy {
        getKoin().getOrNull()
    }

    val uiState = _uiState.asStateFlow()

    private val route: Acknowledgement = savedStateHandle.toRoute()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                    hideAppBar = true
                )
            }

            delay(2000)
            val getDeferredDeepLinkUseCaseVal = getDeferredDeepLinkUseCase

            val deferredDeepLink = if(
                getDeferredDeepLinkUseCaseVal != null &&
                !settings.getBoolean(KEY_DEFERRED_DEEP_LINK_CHECK_DONE, false)
            ) {
                Napier.i("DeferredDeepLink: AcknowledgementViewModel checking for deferred deep link")
                withTimeoutOrNull(GET_DEFERRED_LINK_TIMEOUT) {
                    getDeferredDeepLinkUseCaseVal().also { deferredDeepLink ->
                        Napier.i("DeferredDeepLink: AcknowledgementViewModel invoked: deferredDeepLink=$deferredDeepLink")
                        settings.putBoolean(KEY_DEFERRED_DEEP_LINK_CHECK_DONE, true)
                    }
                }
            }else{
                Napier.i("DeferredDeepLink: AcknowledgementViewModel : no use case available")
                null
            }

            Napier.i("DeferredDeepLink: AcknowledgementViewModel creating initLinkNavCommand: deferredDeepLink=$deferredDeepLink")
            val initLinkNavCommand: NavCommand? = try {
                (deferredDeepLink ?: initDeepLinkUriProvider())?.let { initDeepLink ->
                    resolveUrlToNavCommandUseCase(
                        url = customDeepLinkToUrlUseCase(Url(initDeepLink)),
                        canGoBack = false,
                    )
                }
            }catch(e: Throwable){
                Napier.w("Exception handling initial link", e)
                null
            }

            Napier.i("DeferredDeepLink: AcknowledgementViewModel: init nav command=$initLinkNavCommand")
            val isChild = accountManager.selectedAccountAndPersonFlow.first()?.isChild == true
            val hasAccount = accountManager.activeAccount != null

            _navCommandFlow.tryEmit(
                value = initLinkNavCommand?.withClearBackstack(true) ?: NavCommand.Navigate(
                    destination = when {
                        shouldShowOnboardingUseCase() -> Onboarding
                        hasAccount -> if (isChild) AssignmentList else RespectAppLauncher()
                        route.schoolUrl != null -> AcceptInvite.create(
                            schoolUrl = route.schoolUrl,
                            code = route.inviteCode.toString()
                        )
                        else -> GetStartedScreen()
                    },
                    clearBackStack = true,
                )
            )
        }
    }

    companion object {

        private const val GET_DEFERRED_LINK_TIMEOUT = 1_000L

        const val KEY_DEFERRED_DEEP_LINK_CHECK_DONE = "deferredDeepLinkCheckDone"

    }
}
