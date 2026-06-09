package world.respect.shared.domain.account.invite

import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Clock

/**
     * Deletes the invite if it's a first user invite (firstUser = true)
     * This ensures the first user invite can never be used again after the first user signs up
     */
    suspend fun markFirstUserInviteAsDeleted(
    redeemedInvite: Invite2,
    schoolDataSourceVal: SchoolDataSourceLocal
    ) {
        // Check if this is a NewUserInvite with firstUser = true
        if (redeemedInvite is NewUserInvite && redeemedInvite.firstUser) {

            val deletedInvite = redeemedInvite.copy(
                status = StatusEnum.TO_BE_DELETED,
                lastModified = Clock.System.now()
            )

            schoolDataSourceVal.inviteDataSource.store(listOf(deletedInvite))
        }
    }