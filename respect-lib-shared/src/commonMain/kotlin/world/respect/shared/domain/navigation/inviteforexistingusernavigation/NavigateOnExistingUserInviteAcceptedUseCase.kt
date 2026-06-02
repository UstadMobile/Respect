package world.respect.shared.domain.navigation.inviteforexistingusernavigation

import kotlinx.coroutines.flow.MutableSharedFlow
import world.respect.datalayer.school.ext.isApprovalRequiredNow
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.FamilyMemberInvite
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.navigation.AccountList
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail

class NavigateOnExistingUserInviteAcceptedUseCase {

    operator fun invoke(
        person: Person?,
        parentUid: String? = null,
        inviteRequest: RespectRedeemInviteRequest,
        navCommandFlow: MutableSharedFlow<NavCommand>,
    ) {
        val approvalRequired = inviteRequest.invite.isApprovalRequiredNow()
        val destination = when (val invite = inviteRequest.invite) {

            is ClassInvite -> {

                if (approvalRequired) {
                    AccountList()
                } else {
                    ClazzDetail(
                        guid = invite.classUid
                    )
                }
            }

            is FamilyMemberInvite -> {
                if (person?.status == PersonStatusEnum.PENDING_APPROVAL) {
                    AccountList()
                } else {
                    PersonDetail(
                        guid = parentUid ?: invite.personUid
                    )
                }
            }

            else -> AccountList()
        }

        navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = destination,
                clearBackStack = true
            )
        )
    }
}