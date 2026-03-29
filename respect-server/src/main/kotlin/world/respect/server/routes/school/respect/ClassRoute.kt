package world.respect.server.routes.school.respect

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.http.school.DataAndChangeHistory
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Clazz
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

@Suppress("FunctionName")
fun Route.ClassRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(ClassDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondOffsetLimitPaging(
            params = call.request.queryParameters.offsetLimitPagingLoadParams(),
            pagingSource = schoolDataSource(call).classDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = ClassDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            ).invoke()
        )
    }

    post(ClassDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)

        when (val incoming = call.receive<JsonElement>()) {

            is JsonArray -> {
                val clazz = Json.decodeFromJsonElement<List<Clazz>>(incoming)

                schoolDataSource.classDataSource.store(clazz)
            }

            is JsonObject -> {
                val request = Json.decodeFromJsonElement<DataAndChangeHistory<Clazz>>(incoming)

                schoolDataSource.classDataSource.store(request.data)

                if (request.changeHistories.isNotEmpty()) {
                    schoolDataSource.changeHistoryDataSource.store(request.changeHistories)
                    schoolDataSource.changeHistoryDataSource.markSentToServer(request.changeHistories)
                }
            }

            else -> {
                throw IllegalArgumentException("Invalid request format")
            }
        }

        call.respond(HttpStatusCode.NoContent)
    }

}