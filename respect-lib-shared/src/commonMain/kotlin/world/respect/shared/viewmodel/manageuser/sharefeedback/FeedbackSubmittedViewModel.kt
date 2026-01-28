package world.respect.shared.viewmodel.manageuser.sharefeedback

import androidx.lifecycle.SavedStateHandle
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.viewmodel.RespectViewModel

class FeedbackSubmittedViewModel(
    accountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireActiveAccountScope()

}