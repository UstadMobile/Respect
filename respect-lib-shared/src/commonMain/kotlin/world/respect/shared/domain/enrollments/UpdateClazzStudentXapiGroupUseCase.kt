package world.respect.shared.domain.enrollments

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ext.asXapiAgent
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.ext.mostRecentByTimestampOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.shared.ext.studentsXapiGroup
import kotlin.uuid.ExperimentalUuidApi

class UpdateClazzStudentXapiGroupUseCase(
    private val schoolDataSource: SchoolDataSource,
    private val authenticatedUserPrincipalId: AuthenticatedUserPrincipalId,
    private val schoolUrl: Url,
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        clazzUid: String,
    ) {
        val studentsInClass = schoolDataSource.personDataSource.list(
            loadParams = DataLoadParams(),
            params = PersonDataSource.GetListParams(
                filterByClazzUid = clazzUid,
                filterByEnrolmentRole = EnrollmentRoleEnum.STUDENT,
            )
        ).dataOrNull()

        val classStatement = schoolDataSource.xapiResource.statements.get(
            listParams = GetStatementParams(activity = clazzUid),
            dataLoadParams = DataLoadParams(),
        ).dataOrNull()?.statements?.mostRecentByTimestampOrNull()

        val classActivity = classStatement?.`object` as? XapiActivity

        val studentsXapiGroup = classActivity?.studentsXapiGroup()

        val activePerson = schoolDataSource.personDataSource.findByGuid(
            loadParams = DataLoadParams(onlyIfCached = true),
            guid = authenticatedUserPrincipalId.guid,
        ).dataOrNull()


        if (studentsInClass == null || studentsXapiGroup == null || activePerson == null) {
            Napier.w("No enrollments: something bad: students=$studentsInClass clazz=$classStatement")
            return
        }

        val studentsGroup = studentsXapiGroup.copy(
            member = studentsInClass.map { it.asXapiAgent(schoolUrl) }
        )

        schoolDataSource.xapiResource.statements.post(
            listOf(
                XapiStatement(
                    actor = activePerson.asXapiAgent(schoolUrl),
                    verb = XapiVerb(id = XapiVerb.ID_SAVED),
                    `object` = studentsGroup
                )
            )
        )
    }
}