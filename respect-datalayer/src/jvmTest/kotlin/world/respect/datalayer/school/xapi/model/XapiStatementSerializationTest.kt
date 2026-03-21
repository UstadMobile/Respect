package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test

class XapiStatementSerializationTest {

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
    fun givenValidStatements_whenDeserialized_shouldLoadWithoutException() {
        val resDir = "/world/respect/datalayer/school/xapi/model/"
        statementNames.forEach { name ->
            try {
                val resourceName = "$resDir$name"
                val testStr = this::class.java.getResourceAsStream(resourceName)!!.bufferedReader()
                    .use { it.readText() }
                Json.decodeFromString(XapiStatement.serializer(), testStr)
            }catch(e: Throwable) {
                throw Exception("Error loading $name", e)
            }

        }
    }
}