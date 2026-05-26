package world.respect.datalayer.db.school.xapi

import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.db.school.testSchoolDb
import world.respect.datalayer.db.school.toDataSource
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.db.school.xapi.adapters.toModel
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.datalayer.school.xapi.ext.addStatementIdIfNotPresent
import world.respect.datalayer.school.xapi.ext.allActors
import world.respect.datalayer.school.xapi.ext.allDefinedActivities
import world.respect.datalayer.school.xapi.ext.allDefinedVerbs
import world.respect.datalayer.school.xapi.ext.distinctMerged
import world.respect.datalayer.school.xapi.ext.idStr
import world.respect.datalayer.school.xapi.ext.resultProgressExtension
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementTransformingSerializer
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.test.res.forXapiSampleStatements
import world.respect.lib.test.res.xapiSampleStatements
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XAPI_RESULT_EXTENSION_PROGRESS
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiResult
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementFormatEnum
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.uuid.Uuid

class XapiStatementsResourceDbTest {


    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    val json = Json

    @Test
    fun givenStatement_whenConvertedToEntitiesAndBack_thenShouldMatch() {
        forXapiSampleStatements { sample ->
            val statement = Json.decodeFromJsonElement(
                XapiStatementTransformingSerializer,
                sample.jsonObject.addStatementIdIfNotPresent(),
            ).let { it.copy(id = it.id ?: Uuid.random()) }

            val uidNumberMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm())

            val statementEntities = statement.toEntities(
                uidNumberMapper = uidNumberMapper,
                json = json,
                isSubStatement = false,
            )

            val timeNow = Clock.System.now()
            val actors = statement.allActors().distinctMerged().map {
                it.toEntities(uidNumberMapper, timeNow)
            }.map {
                it.toModel(idOnlyFormat = false)
            }
            val activities = statement.allDefinedActivities().distinctMerged().mapNotNull {
                it.toEntities(uidNumberMapper, json, timeNow)
            }.map {
                it.toModel(json)
            }
            val verbs = statement.allDefinedVerbs()

            val primaryStatementEntity = statementEntities.statements.first { !it.isSubStatement }
            val statementFromEntities = statementEntities.toModel(
                json = json,
                uidNumberMapper = uidNumberMapper,
                actors = actors,
                activities = activities,
                verbs = verbs,
                statementIdHi = primaryStatementEntity.statementIdHi,
                statementIdLo = primaryStatementEntity.statementIdLo,
            )

            try {
                assertXapiStatementCanonicallyEqual(
                    expected = statement,
                    actual = statementFromEntities
                )
            }catch(e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    @Test
    fun givenStatement_whenStoredAndRetrieved_thenShouldMatch() {
        runBlocking {
            forXapiSampleStatements { statement ->
                testSchoolDb(temporaryFolder.newFolder()) { db ->
                    val dataSource = db.toDataSource(
                        authenticatedUserUid = "1",
                        schoolUrl = Url("http://localhost:8098/"),
                    )

                    val stmtUuid = Uuid.random()
                    val timeNow = Clock.System.now()

                    val statement = Json.decodeFromJsonElement(
                        XapiStatement.serializer(), statement.jsonObject
                    ).copy(
                        id = stmtUuid,
                        timestamp = timeNow,
                        stored = Clock.System.now(),
                    )

                    dataSource.xapiStatementsResource.post(listOf(statement))

                    //check canonical match
                    val canonicalStmtFromDb = dataSource.xapiStatementsResource.get(

                        listParams = XapiStatementsResource.GetStatementParams(
                            format = XapiStatementsResource.GetStatementFormatEnum.CANONICAL,
                            statementId = stmtUuid
                        )
                    ).dataOrNull()?.statements?.first()

                    assertNotNull(canonicalStmtFromDb)
                    assertXapiStatementCanonicallyEqual(
                        expected = statement,
                        actual = canonicalStmtFromDb,
                    )

                    val exactStmtFromDb = dataSource.xapiStatementsResource.get(

                        listParams = XapiStatementsResource.GetStatementParams(
                            format = XapiStatementsResource.GetStatementFormatEnum.EXACT,
                            statementId = stmtUuid
                        )
                    ).dataOrNull()?.statements?.first()
                    assertEquals(statement, exactStmtFromDb)

                    val idOnlyStmtFromDb = dataSource.xapiStatementsResource.get(
                        listParams = XapiStatementsResource.GetStatementParams(
                            format = XapiStatementsResource.GetStatementFormatEnum.IDS,
                            statementId = stmtUuid
                        )
                    ).dataOrNull()?.statements?.first()
                    assertNotNull(idOnlyStmtFromDb)
                    assertXapiStatementCanonicallyEqual(
                        expected = statement,
                        actual = idOnlyStmtFromDb,
                        idOnlyFormat = true,
                    )
                }
            }
        }
    }

    @Test
    fun givenStatementInserted_whenVoided_thenShouldNotShowUp() {
        val sampleStmt = xapiSampleStatements().first()
        runBlocking {
            testSchoolDb(temporaryFolder.newFolder()) { db ->
                val stmtUuid = Uuid.random()
                val statement = Json.decodeFromJsonElement(
                    XapiStatement.serializer(), sampleStmt.jsonObject
                ).copy(id = stmtUuid)

                val dataSource = db.toDataSource(
                    authenticatedUserUid = "1",
                    schoolUrl = Url("http://localhost:8098/"),
                )

                val getStmtParams = XapiStatementsResource.GetStatementParams(
                    statementId = stmtUuid
                )

                dataSource.xapiStatementsResource.post(listOf(statement))

                assertXapiStatementCanonicallyEqual(
                    expected = statement,
                    actual = dataSource.xapiStatementsResource.get(
                        listParams = getStmtParams
                    ).dataOrNull()?.statements?.firstOrNull()!!
                )

                val voidingStatement = XapiStatement(
                    actor = statement.actor,
                    verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                    `object` = XapiStatementRef(id = stmtUuid.toString())
                )
                dataSource.xapiStatementsResource.post(listOf(voidingStatement))

                GetStatementFormatEnum.entries.forEach { format ->
                    assertNull(
                        dataSource.xapiStatementsResource.get(
                            listParams = getStmtParams.copy(
                                format = format
                            )
                        ).dataOrNull()?.statements?.firstOrNull()
                    )
                }

                val getByVoidedParams = XapiStatementsResource.GetStatementParams(
                    voidedStatementId = stmtUuid
                )
                assertEquals(
                    expected = statement,
                    actual = dataSource.xapiStatementsResource.get(
                        getByVoidedParams.copy(format = GetStatementFormatEnum.EXACT)
                    ).dataOrNull()?.statements?.firstOrNull()
                )
                assertXapiStatementCanonicallyEqual(
                    expected = statement,
                    actual = dataSource.xapiStatementsResource.get(
                        getByVoidedParams.copy(format = GetStatementFormatEnum.CANONICAL)
                    ).dataOrNull()?.statements?.firstOrNull()!!
                )
            }
        }
    }


    fun randomNullableBoolean(): Boolean? {
        return when(val i = Random.nextInt(-1 until 2)) {
            -1 -> null
            0 -> false
            1 -> true
            else -> throw IllegalStateException(i.toString())
        }
    }

    @Test
    fun givenAssignmentAndCompletionStatementsMade_whenGetProgressCalled_thenSummariesShouldMatch() {
        runBlocking {
            testSchoolDb(temporaryFolder.newFolder()) { db ->
                val assignmentActivityId = "http://localhost:8098/assignment-activity-id"
                val assignmentTasks = (1..3).map { "http://localhost:8098/assignment/task$it" }

                val dataSource = db.toDataSource(
                    authenticatedUserUid = "1",
                    schoolUrl = Url("http://localhost:8098/"),
                )

                val teacherAgent = XapiAgent(
                    name = "Teacher",
                    account = XapiAccount(
                        homePage = "http://localhost:8098/",
                        name = "teacher"
                    )
                )

                val studentGroup = XapiGroup(
                    name = "Students",
                    account = XapiAccount(
                        homePage = "http://localhost:8098/class/1",
                        name = "students"
                    ),
                    member = (1..5).map {
                        XapiAgent(
                            name = "Student $it",
                            account = XapiAccount(
                                homePage = "http://localhost:8098/student/$it",
                                name = "student$it"
                            )
                        )
                    }
                )

                val createGroupStmt = XapiStatement(
                    actor = teacherAgent,
                    verb = XapiVerb("http://activitystrea.ms/schema/1.0/saved"),
                    `object` = studentGroup,
                )

                dataSource.xapiStatementsResource.post(listOf(createGroupStmt))

                val setAssignmentStmt = XapiStatement(
                    actor = studentGroup.copy(member = null),
                    verb = XapiVerb(id = XapiVerb.ID_ASSIGN),
                    `object` = XapiActivity(
                        id = assignmentActivityId,
                        definition = XapiActivityDefinition(
                            name = mapOf(
                                "en-US" to "Test Assignment"
                            ),
                            type = "http://id.tincanapi.com/activitytype/school-assignment"
                        )
                    ),
                    context = XapiContext(
                        instructor = teacherAgent,
                        contextActivities = XapiContextActivities(
                            grouping = assignmentTasks.map {
                                XapiActivity(
                                    id = it,
                                )
                            }
                        )
                    )
                )

                dataSource.xapiStatementsResource.post(listOf(setAssignmentStmt))

                val progressStatements = studentGroup.member!!.flatMap { studentActor ->
                    val studentCompletedAll = Random.nextBoolean()

                    assignmentTasks.map { assignmentTaskId ->
                        val isComplete = if(studentCompletedAll){
                            true
                        }else {
                            randomNullableBoolean()
                        }

                        XapiStatement(
                            actor = studentActor,
                            verb = XapiVerb(
                                id = if(isComplete == true) {
                                    XapiVerb.ID_COMPLETED
                                }else {
                                    XapiVerb.ID_EXPERIENCED
                                }
                            ),
                            `object` = XapiActivity(id = assignmentTaskId),
                            result = XapiResult(
                                score = XapiResult.Score(
                                    scaled = Random.nextDouble(
                                        0.toDouble(), 1.toDouble()
                                    ).toFloat()
                                ),
                                success = randomNullableBoolean(),
                                completion = isComplete,
                                extensions = JsonObject(
                                    mapOf(
                                        XAPI_RESULT_EXTENSION_PROGRESS to JsonPrimitive(Random.nextInt(100))
                                    )
                                )
                            ),
                            context = XapiContext(
                                contextActivities = XapiContextActivities(
                                    grouping = listOf(
                                        XapiActivity(id = assignmentActivityId)
                                    )
                                )
                            )
                        )
                    }
                }.also {
                    dataSource.xapiStatementsResource.post(it)
                }

                val assignmentResults = dataSource.xapiStatementsResource.getAssignmentProgress(
                    assignmentActivityId
                ).first().dataOrNull()

                assertNotNull(assignmentResults)

                studentGroup.member!!.forEach { student ->
                    assignmentTasks.forEach { taskActivityId ->
                        val result = assignmentResults.progress.first {
                            it.actor.idStr == student.idStr //canoncical comparison
                        }.progressPerTask.first {
                            it.activityId == taskActivityId
                        }

                        val statement = progressStatements.first {
                            val stmtActivity = it.`object` as? XapiActivity
                            it.actor == student && stmtActivity?.id == taskActivityId
                        }

                        assertEquals(
                            statement.result?.success,
                            result.successful
                        )

                        assertEquals(
                            statement.result?.completion,
                            result.completed,
                        )

                        assertEquals(
                            statement.result?.score?.scaled,
                            result.scoreScaled,
                        )

                        assertEquals(
                            statement.resultProgressExtension,
                            result.progress,
                        )
                    }
                }

                val summaries = dataSource.xapiStatementsResource.getAssignmentListAsFlow(
                    dataLoadParams = DataLoadParams(),
                    studentAgent = null,
                ).first().dataOrNull()

                assertNotNull(summaries)

                val summaryForAssignment = summaries.firstOrNull {
                    it.activityId == assignmentActivityId
                }

                assertNotNull(summaryForAssignment)

                assertEquals(
                    studentGroup.member!!.size,
                    summaryForAssignment.totalCount
                )

                assertEquals(
                    expected = studentGroup.member!!.count { studentAgent ->
                        assignmentTasks.all { assignedUnitId ->
                            progressStatements.any {
                                it.actor == studentAgent &&
                                        it.objectActivityOrNull()?.id == assignedUnitId &&
                                        it.verb.id == XapiVerb.ID_COMPLETED
                            }
                        }
                    },
                    actual = summaryForAssignment.completedCount
                )
            }
        }
    }
}