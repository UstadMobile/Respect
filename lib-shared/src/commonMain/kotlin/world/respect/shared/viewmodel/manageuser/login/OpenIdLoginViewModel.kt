package world.respect.shared.viewmodel.manageuser.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.account.authwithopenid.OpenIdAuthorizationUseCase
import world.respect.shared.domain.account.authwithopenid.VerifyOpenIdTokenUseCase
import world.respect.shared.navigation.OpenIdLogin
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

class OpenIdLoginViewModel(
    savedStateHandle: SavedStateHandle,
    private val openIdAuthorizationUseCase: OpenIdAuthorizationUseCase,
    private val verifyOpenIdTokenUseCase: VerifyOpenIdTokenUseCase,
) : RespectViewModel(savedStateHandle) {

    private val route: OpenIdLogin = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = route.providerName.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }
    }

    fun onClickContinue() {
        viewModelScope.launch {
            val result = openIdAuthorizationUseCase(route.issuerUrl)

            result.accessToken?.let { accessToken ->
                val serverVerified = verifyOpenIdTokenUseCase(
                    schoolUrl = route.schoolUrl,
                    accessToken = accessToken,
                )
                Napier.d("verified $serverVerified")
            }
        }
    }
}
