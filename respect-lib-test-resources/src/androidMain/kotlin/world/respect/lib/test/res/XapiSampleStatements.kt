package world.respect.lib.test.res

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

fun xapiSampleStatements(context: Context): List<SampleXapiStatement> {
    val assetDirName = "xapistatements"
    return STATEMENT_NAMES.map { name ->
        context.assets.open("$assetDirName/$name").bufferedReader().use {
            val jsonStr = it.readText()

            SampleXapiStatement(
                jsonObject = Json.decodeFromString(JsonObject.serializer(),jsonStr),
                string = jsonStr,
                name = name,
            )
        }
    }
}
