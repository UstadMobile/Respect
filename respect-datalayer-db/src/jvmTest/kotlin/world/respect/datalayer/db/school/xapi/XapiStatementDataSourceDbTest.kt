package world.respect.datalayer.db.school.xapi

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.db.school.testSchoolDb
import world.respect.datalayer.db.school.toDataSource
import world.respect.datalayer.school.xapi.model.XapiStatement
import java.io.File
import kotlin.test.Test

class XapiStatementDataSourceDbTest {


    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()


    val statementNames = listOf(
        "appendix-a-long-statement.json",
        "group-statement.json",
        "simple-statement.json",
        "likert-response-statement.json",
        "matching-response-statement.json",
        "multi-choice-statement-invalid-response.json",
        "multi-choice-statement.json",
        "multi-choice-statement-multiple-responses.json",
        "performance-response-statement.json",
        "sequencing-response-statement.json",
        "statement-with-object-actor.json",
        "statement-with-object-statementref.json",
        "statement-with-object-substatement.json",
        "true-false-response-statement.json",
    )


    @Test
    fun givenStatement_canStore() {
        val resDir = "/world/respect/datalayer/school/xapi/model/"
        runBlocking {
            testSchoolDb(temporaryFolder.newFolder()) { db ->
                val dataSource = db.toDataSource(
                    authenticatedUserUid = "1",
                    schoolUrl = Url("http://localhost:8098/"),
                )

                statementNames.forEach { name ->
                    try {
                        val resourceName = "$resDir$name"
                        val testStr = this::class.java.getResourceAsStream(resourceName)!!.bufferedReader()
                            .use { it.readText() }
                        val statement = Json.decodeFromString(
                            XapiStatement.serializer(), testStr
                        )

                        dataSource.xapiStatementDataSource.store(listOf(statement))
                    }catch(e: Throwable) {
                        throw Exception("Error loading/storing $name", e)
                    }
                }
            }
        }
    }

}