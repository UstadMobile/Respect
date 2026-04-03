package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.json.Json
import world.respect.lib.test.res.forXapiSampleStatements
import kotlin.test.Test

class XapiStatementSerializationTest {

    @Test
    fun givenValidStatements_whenDeserialized_shouldLoadWithoutException() {
        forXapiSampleStatements { sampleStmt ->
            Json.decodeFromJsonElement(XapiStatement.serializer(), sampleStmt)
        }
    }
}