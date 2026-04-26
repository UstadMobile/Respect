package world.respect.datalayer.db.school.xapi

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.db.school.testSchoolDb
import world.respect.datalayer.db.school.toDataSource
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.db.school.xapi.adapters.toModel
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.xapi.XapiStatementDataSource
import world.respect.datalayer.school.xapi.ext.addStatementIdIfNotPresent
import world.respect.datalayer.school.xapi.ext.allActors
import world.respect.datalayer.school.xapi.ext.allDefinedActivities
import world.respect.datalayer.school.xapi.ext.allDefinedVerbs
import world.respect.datalayer.school.xapi.ext.distinctMerged
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.school.xapi.model.XapiStatementTransformingSerializer
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.test.res.forXapiSampleStatements
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock
import kotlin.uuid.Uuid

class XapiStatementDataSourceDbTest {


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
    fun givenStatement_canStoreAndRetrieve() {
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

                    dataSource.xapiStatementDataSource.store(listOf(statement))

                    //check canonical match
                    val canonicalStmtFromDb = dataSource.xapiStatementDataSource.list(
                        listParams = XapiStatementDataSource.GetStatementParams(
                            format = XapiStatementDataSource.GetStatementFormatEnum.EXACT,
                            statementId = stmtUuid
                        )
                    ).dataOrNull()?.first()

                    assertNotNull(canonicalStmtFromDb)
                    assertXapiStatementCanonicallyEqual(
                        expected = statement,
                        actual = canonicalStmtFromDb,
                    )

                    val exactStmtFromDb = dataSource.xapiStatementDataSource.list(
                        listParams = XapiStatementDataSource.GetStatementParams(
                            format = XapiStatementDataSource.GetStatementFormatEnum.EXACT,
                            statementId = stmtUuid
                        )
                    ).dataOrNull()?.first()
                    assertEquals(statement, exactStmtFromDb)

                    val idOnlyStmtFromDb = dataSource.xapiStatementDataSource.list(
                        listParams = XapiStatementDataSource.GetStatementParams(
                            format = XapiStatementDataSource.GetStatementFormatEnum.IDS,
                            statementId = stmtUuid
                        )
                    ).dataOrNull()?.first()
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

}