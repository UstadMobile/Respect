package world.respect.shared.domain.navigation.inviteforexistingusernavigation

import kotlinx.coroutines.flow.MutableSharedFlow
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.navigation.AccountList
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.NavCommand

class NavigateOnExistingUserInviteAcceptedUseCase() {

    operator fun invoke(
        person: Person?,
        inviteRequest: RespectRedeemInviteRequest,
        navCommandFlow: MutableSharedFlow<NavCommand>,
    ) {
        val destination = when (val invite = inviteRequest.invite) {

            is ClassInvite -> {
                val approvalRequired = person?.status == PersonStatusEnum.PENDING_APPROVAL

                if (approvalRequired) {
                    AccountList
                } else {
                    ClazzDetail(
                        guid = invite.classUid
                    )
                }
            }

            else -> {
                AccountList
            }
        }

        navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = destination,
                clearBackStack = true
            )
        )
    }
}