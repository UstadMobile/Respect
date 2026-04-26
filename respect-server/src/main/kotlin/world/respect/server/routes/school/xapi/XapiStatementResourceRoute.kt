package world.respect.server.routes.school.xapi

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.json.Json
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.xapi.XapiStatementDataSource
import world.respect.lib.xapi.XapiRequestHeaders
import world.respect.server.util.ext.requireAccountScope

fun Route.XapiStatementResourceRoute(
    statementResource: (ApplicationCall) -> XapiStatementDataSource = { call ->
        call.requireAccountScope().get<SchoolDataSource>().xapiStatementDataSource
    },
    json: Json,
) {
    get("statements") {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val statementResponse = statementResource(call).get(
            request = XapiStatementDataSource.GetStatementsRequest(
                params = XapiStatementDataSource.GetStatementParams.fromParams(
                    params = call.request.queryParameters,
                    json = json,
                ),
                headers = XapiRequestHeaders()
            )
        )

        call.respond(statementResponse.statementResult)
    }
}