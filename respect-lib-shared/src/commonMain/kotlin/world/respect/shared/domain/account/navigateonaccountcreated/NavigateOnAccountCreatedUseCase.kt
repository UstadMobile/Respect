package world.respect.shared.domain.account.navigateonaccountcreated

import kotlinx.coroutines.flow.MutableSharedFlow
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.WaitingForApproval

/**
 * Decide where to navigate after a user account has been created.
 */
class NavigateOnAccountCreatedUseCase {

    operator fun invoke(
        personRegistered: Person,
        navCommandFlow: MutableSharedFlow<NavCommand>,
    ) {
        navCommandFlow.tryEmit(
            value = NavCommand.Navigate(
                destination = if(personRegistered.status == PersonStatusEnum.PENDING_APPROVAL) {
                    WaitingForApproval()
                }else {
                    RespectAppLauncher()
                },
                clearBackStack = true
            )
        )
    }

}