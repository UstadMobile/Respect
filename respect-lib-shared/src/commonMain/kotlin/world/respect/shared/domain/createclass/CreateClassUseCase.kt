package world.respect.shared.domain.createclass

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Invite2

/**
 * Use case to contain logic for creating invites for a new class.
 *
 * Creates invites for each role (teacher direct, student direct, student via parent).
 *
 * @property dataSource the SchoolDataSource used to store invites
 */
class CreateClassUseCase(
    private val dataSource: SchoolDataSource
) {
    suspend operator fun invoke(
        classActivityId: String
    ) {
        dataSource.inviteDataSource.store(
            listOf(
                Pair(EnrollmentRoleEnum.TEACHER, ClassInviteModeEnum.DIRECT),
                Pair(EnrollmentRoleEnum.STUDENT, ClassInviteModeEnum.DIRECT),
                Pair(EnrollmentRoleEnum.STUDENT, ClassInviteModeEnum.VIA_PARENT),
            ).map { (role, inviteMode) ->
                ClassInvite(
                    uid = ClassInvite.uidFor(
                        classActivityId, role, inviteMode = inviteMode
                    ),
                    code = Invite2.newRandomCode(),
                    classUid = classActivityId,
                    role = role,
                    inviteMode = inviteMode,
                )
            }
        )
    }
}