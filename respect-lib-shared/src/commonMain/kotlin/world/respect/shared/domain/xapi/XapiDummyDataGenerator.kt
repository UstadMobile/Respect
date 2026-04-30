package world.respect.shared.domain.xapi

import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.Person
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiResult
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.datalayer.db.school.ext.fullName
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.time.Clock

@OptIn(ExperimentalUuidApi::class)
class XapiDummyDataGenerator {
    
    fun generateDummyStatements(
        students: List<Person>,
        assignment: Assignment,
        schoolUrl: String
    ): List<XapiStatement> {
        val assignmentActivityId = "$schoolUrl/assignment/${assignment.uid}"
        
        return students.flatMap { student ->
            assignment.learningUnits.map { ref ->
                XapiStatement(
                    id = Uuid.random(),
                    actor = XapiAgent(
                        name = student.fullName(),
                        account = XapiAccount(
                            homePage = schoolUrl,
                            name = student.fullName()
                        )
                    ),
                    verb = XapiVerb(
                        id = "http://adlnet.gov/expapi/verbs/completed",
                        display = mapOf("en-US" to "completed")
                    ),
                    `object` = XapiActivity(
                        id = ref.learningUnitManifestUrl.toString(),
                        definition = XapiActivityDefinition(
                            name = mapOf("en-US" to "Learning Unit"),
                            description = mapOf("en-US" to "A learning unit in the assignment")
                        )
                    ),
                    result = XapiResult(
                        score = XapiResult.Score(scaled = 0.95F, raw = 95.0F, min = 0.0F, max = 100.0F),
                        success = true,
                        completion = true,
                        duration = null
                    ),
                    context = XapiContext(
                        contextActivities = XapiContextActivities(
                            grouping = listOf(XapiActivity(id = assignmentActivityId, objectType = XapiObjectType.Activity))
                        )
                    ),
                    timestamp = Clock.System.now(),
                    stored = Clock.System.now()
                )
            }
        }
    }
}
