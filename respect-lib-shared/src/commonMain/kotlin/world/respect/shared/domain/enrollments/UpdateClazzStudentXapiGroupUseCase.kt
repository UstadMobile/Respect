package world.respect.shared.domain.enrollments

import io.ktor.http.Url
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ext.asXapiAgent
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.ext.mostRecentByTimestampOrNull
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
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
        val className = classActivity?.definition?.name?.values?.firstOrNull()

        val studentsXapiGroup = if (classActivity != null) {
            XapiGroup(
                name = "${className ?: ""} students",
                account = XapiAccount(
                    homePage = classActivity.id,
                    name = "students"
                ),
                objectType = XapiObjectType.Group,
            )
        } else {
            null
        }

        val activePerson = schoolDataSource.personDataSource.findByGuid(
            loadParams = DataLoadParams(onlyIfCached = true),
            guid = authenticatedUserPrincipalId.guid,
        ).dataOrNull()

        println("UpdateClazzStudentXapiGroup: clazzUid='$clazzUid'")
        println("UpdateClazzStudentXapiGroup: studentsInClass=${studentsInClass?.map { "${it.givenName} ${it.familyName}" }}")
        println("UpdateClazzStudentXapiGroup: classActivity=${classActivity?.id}")
        println("UpdateClazzStudentXapiGroup: className=$className")
        println("UpdateClazzStudentXapiGroup: studentsXapiGroup=$studentsXapiGroup")
        println("UpdateClazzStudentXapiGroup: activePerson=${activePerson?.let { "${it.givenName} ${it.familyName}" }}")

        if(studentsInClass == null || studentsXapiGroup == null || activePerson == null) {
            println("UpdateClazzStudentXapiGroup: RETURNING EARLY - studentsInClass=${studentsInClass != null}, studentsXapiGroup=${studentsXapiGroup != null}, activePerson=${activePerson != null}")
            return
        }

        val classDisplayName = className ?: "Unknown Class"
        val studentNames = studentsInClass.map { "${it.givenName} ${it.familyName}" }
        println("UpdateClazzStudentXapiGroup: Class='$classDisplayName', Students=$studentNames")

        val groupToPost = studentsXapiGroup.copy(
            member = studentsInClass.map { it.asXapiAgent(schoolUrl) }
        )
        println("UpdateClazzStudentXapiGroup: Posting XapiGroup name='${groupToPost.name}', account=${groupToPost.account}, members=${groupToPost.member?.map { it.name }}")

        schoolDataSource.xapiResource.statements.post(
            listOf(
                XapiStatement(
                    actor = activePerson.asXapiAgent(schoolUrl),
                    verb = XapiVerb(id = XapiVerb.ID_SAVED),
                    `object` = groupToPost
                )
            )
        )
    }

}