package world.respect.lib.test.res

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

data class DummyClass(val num: Int)



val RES_DIR = "/world/respect/datalayer/school/xapi/model/"

fun xapiSampleStatements(): List<SampleXapiStatement> {
    return STATEMENT_NAMES.map { name ->
        val resourceName = "$RES_DIR$name"
        val statementStr = DummyClass::class.java.getResourceAsStream(resourceName)!!.bufferedReader()
            .use { it.readText() }

        SampleXapiStatement(
            name = name,
            string = statementStr,
            jsonObject = Json.decodeFromString<JsonObject>(statementStr)
        )
    }
}

inline fun forXapiSampleStatements(
    block: (SampleXapiStatement) -> Unit
) {
    xapiSampleStatements().forEach(block)
}
