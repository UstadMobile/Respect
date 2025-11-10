package world.respect.shared.viewmodel.enrollment.list

import androidx.lifecycle.SavedStateHandle
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.viewmodel.RespectViewModel

class EnrollmentListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()
}