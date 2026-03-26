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
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.domain.GetPermissionLastModifiedUseCase
import world.respect.datalayer.school.model.Enrollment
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

fun Route.EnrollmentRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
    getPermissionLastModifiedUseCase: (ApplicationCall) -> GetPermissionLastModifiedUseCase = { call ->
        call.requireAccountScope().get()
    }
) {
    get(EnrollmentDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondOffsetLimitPaging(
            params = call.request.queryParameters.offsetLimitPagingLoadParams(),
            pagingSource = schoolDataSource(call).enrollmentDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                listParams = EnrollmentDataSource.GetListParams.fromParams(call.request.queryParameters)
            ).invoke(),
            getPermissionLastModifiedUseCase = getPermissionLastModifiedUseCase(call),
        )
    }

    post(EnrollmentDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)

        when (val incoming = call.receive<JsonElement>()) {

            is JsonArray -> {
                val enrollments = Json.decodeFromJsonElement<List<Enrollment>>(incoming)

                schoolDataSource.enrollmentDataSource.store(enrollments)
            }

            is JsonObject -> {
                val request = Json.decodeFromJsonElement<DataAndChangeHistory<Enrollment>>(incoming)

                schoolDataSource.enrollmentDataSource.store(request.data)

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