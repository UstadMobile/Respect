package world.respect.shared.domain.account.invite

import androidx.paging.PagingSource
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.model.EnrollmentRoleEnum
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
        classUid: String,
        approved: Boolean,
    ) {
        val person = schoolDataSource.personDataSource.findByGuid(
            DataLoadParams(), personUid
        ).dataOrNull() ?: throw IllegalArgumentException("Cant find person")

        val enrollments = schoolDataSource.enrollmentDataSource.listAsPagingSource(
            DataLoadParams(),
            EnrollmentDataSource.GetListParams(
                classUid = classUid,
                personUid = personUid,
            )
        ).load(PagingSource.LoadParams.Refresh(0, 20, false))
            as? PagingSource.LoadResult.Page
        val pendingEnrollment = enrollments?.data?.firstOrNull {
            it.role == EnrollmentRoleEnum.PENDING_TEACHER || it.role == EnrollmentRoleEnum.PENDING_STUDENT
        } ?: throw IllegalArgumentException("Can't find pending enrollment")

        val timeModified = Clock.System.now()
        schoolDataSource.personDataSource.store(
            listOf(
                person.copy(status = PersonStatusEnum.ACTIVE, lastModified = timeModified)
            )
        )
        schoolDataSource.enrollmentDataSource.store(
            listOf(
                pendingEnrollment.copy(
                    role = if(pendingEnrollment.role == EnrollmentRoleEnum.PENDING_TEACHER) {
                        EnrollmentRoleEnum.TEACHER
                    } else {
                        EnrollmentRoleEnum.STUDENT
                    },
                    lastModified = timeModified,
                )
            )
        )
    }

}