package world.respect.shared.domain.account.invite

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.PersonStatusEnum
import kotlin.time.Clock


/**
 * Could take the accountmanager as a dependency: any API calls need to be linked to a specific
 * account
 */
class ApproveOrDeclineInviteRequestUseCase(
    private val schoolDataSource: SchoolDataSource,
) {

    /**
     * @param approved true/false
     */
    suspend operator fun invoke(
        personUid: String,
        approved: Boolean,
    ) {
        val person = schoolDataSource.personDataSource.findByGuid(
            DataLoadParams(), personUid
        ).dataOrNull() ?: throw IllegalArgumentException("Can't find person")

        val timeNow = Clock.System.now()

        schoolDataSource.personDataSource.store(
            listOf(
                person.copy(
                    status = if(approved) {
                        PersonStatusEnum.ACTIVE
                    }else {
                        PersonStatusEnum.TO_BE_DELETED
                    },
                    lastModified = timeNow,
                )
            )
        )
    }

}