package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.feedback_submitted
import world.respect.shared.navigation.FeedbackSubmitted
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class FeedbackSubmittedUiState(
    val ticketId: Int? = null
)

class FeedbackSubmittedViewModel(
    accountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireActiveAccountScope()

    val route: FeedbackSubmitted = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(FeedbackSubmittedUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.feedback_submitted.asUiText(),
                hideBottomNavigation = true
            )
        }
        _uiState.update {
            it.copy(
                ticketId = route.ticketId
            )
        }
    }
}