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
import world.respect.datalayer.school.xapi.ext.addStatementIdIfNotPresent
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.school.xapi.model.XapiStatementTransformingSerializer
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.test.res.forXapiSampleStatements
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import kotlin.test.Test
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

            val entities = statement.toEntities(
                uidNumberMapper = uidNumberMapper,
                json = json,
                exactJson = null,
                isSubStatement = false,
            )

            val primaryStatementEntity = entities.statements.first { !it.isSubStatement }
            val statementFromEntities = entities.toModel(
                json = json,
                statementIdHi = primaryStatementEntity.statementIdHi,
                statementIdLo = primaryStatementEntity.statementIdLo,
            )

            try {
                assertXapiStatementMatches(
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
    fun givenStatement_canStore() {
        runBlocking {
            testSchoolDb(temporaryFolder.newFolder()) { db ->
                val dataSource = db.toDataSource(
                    authenticatedUserUid = "1",
                    schoolUrl = Url("http://localhost:8098/"),
                )

                forXapiSampleStatements { statement ->
                    val statement = Json.decodeFromJsonElement(
                        XapiStatement.serializer(), statement.jsonObject
                    )

                    dataSource.xapiStatementDataSource.store(listOf(statement))
                }
            }
        }
    }

}