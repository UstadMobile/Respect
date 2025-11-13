package world.respect.server.routes.school.respect

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase

fun Route.RunReportRoute(
    runReportUseCaseProvider: (ApplicationCall) -> RunReportUseCase,
    json: Json,
) {
    post("run") {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        val runReportUseCase = runReportUseCaseProvider(call)

        val requestTxt = call.receiveText()
        val request = json.decodeFromString(
            RunReportUseCase.RunReportRequest.serializer(), requestTxt
        )

        val result = runReportUseCase.invoke(request).first()

        call.respondText(
            contentType = ContentType.Application.Json,
            text = json.encodeToString(
                RunReportUseCase.RunReportResult.serializer(), result
            )
        )
    }
}