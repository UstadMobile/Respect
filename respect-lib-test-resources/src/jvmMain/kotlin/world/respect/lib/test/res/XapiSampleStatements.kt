package world.respect.lib.test.res

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

data class DummyClass(val num: Int)


val STATEMENT_NAMES = listOf(
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

val RES_DIR = "/world/respect/datalayer/school/xapi/model/"

inline fun forXapiSampleStatements(
    block: (JsonObject) -> Unit
) {
    STATEMENT_NAMES.forEach { name ->
        try {
            val resourceName = "$RES_DIR$name"
            val statementStr =DummyClass::class.java.getResourceAsStream(resourceName)!!.bufferedReader()
                .use { it.readText() }
            block(
                Json.decodeFromString<JsonObject>(statementStr)
            )
        }catch(e: Throwable) {
            throw Exception("Error w/statement: $name", e)
        }

    }

}
