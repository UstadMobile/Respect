package world.respect.datalayer.repository.school.pullsync

import io.github.aakira.napier.Napier
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.PullSyncStatus
import world.respect.datalayer.shared.PullSyncTracker
import world.respect.datalayer.shared.params.GetListCommonParams
import java.lang.IllegalStateException
import kotlin.time.Instant

/**
 * Handling permissions requires that all available enrollments for the authenticated user are kept
 * up to date: Class based permissions may be granted by role (e.g. to teachers of the class or to
 * teachers of the class).
 *
 * This is needed not only to determine which permissions are granted to the user, but also their
 * scope : eg. a student (and therefor their parent) is by default granted the PERSON_STUDENT_READ
 * permission for the class. This means they can view the names of students in their class. For the
 * permission query to work, it needs the EnrollmentEntity for both the authenticated user _and_ all
 * others in the class.
 *
 * Should probably be done using WorkManager...
 */
class RunPullSyncUseCase(
    private val pullSyncTracker: PullSyncTracker,
    private val schoolDataSource: SchoolDataSource,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) {

    suspend operator fun invoke() {
        val tableId = Enrollment.TABLE_ID

        val status = pullSyncTracker.getPullSyncStatus(tableId) ?: PullSyncStatus(
            accountPersonUid = authenticatedUser.guid,
            consistentThrough = Instant.fromEpochMilliseconds(0),
            tableId = tableId,
        )

        var since: Instant = status.consistentThrough

        do {
            Napier.d("PullSync: Loading since $since")
            val loadResult = schoolDataSource.enrollmentDataSource.list(
                loadParams = DataLoadParams(),
                listParams = EnrollmentDataSource.GetListParams(
                    common = GetListCommonParams(
                        since = since,
                    )
                )
            )

            if(loadResult.remoteState !is DataReadyState) {
                throw IllegalStateException("Could not load remote data")
            }

            since = loadResult.remoteState?.metaInfo?.consistentThrough ?: throw IllegalStateException(
                "RunPullSyncUseCase: Load result MUST have consistentThrough"
            )
        }while (loadResult.dataOrNull()?.isNotEmpty() == true)

        Napier.d("PullSync: Done consistent through to $since")
        pullSyncTracker.updatePullSyncStatus(
            status.copy(consistentThrough = since)
        )
    }

}