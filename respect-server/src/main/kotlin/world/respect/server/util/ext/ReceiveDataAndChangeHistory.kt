package world.respect.server.util.ext

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.http.school.DataAndChangeHistory

suspend fun <T> ApplicationCall.receiveDataAndChangeHistory(
    json: Json,
    listDeserializer: DeserializationStrategy<List<T>>,
    wrapperDeserializer: DeserializationStrategy<DataAndChangeHistory<T>>
): DataAndChangeHistory<T> {

    return when (val incoming = receive<JsonElement>()) {

        is JsonArray -> {
            val data = json.decodeFromJsonElement(listDeserializer, incoming)
            DataAndChangeHistory(
                data = data,
                changeHistories = emptyList()
            )
        }

        is JsonObject -> {
            json.decodeFromJsonElement(wrapperDeserializer, incoming)
        }

        else -> {
            throw IllegalArgumentException("Invalid request format")
        }
    }
}