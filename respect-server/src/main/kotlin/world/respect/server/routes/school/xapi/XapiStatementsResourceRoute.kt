package world.respect.server.routes.school.xapi

import io.ktor.http.HttpHeaders
import io.ktor.http.toHttpDate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.date.GMTDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiSingleItemToListSerializer
import world.respect.lib.xapi.model.XapiStatement
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondDataLoadState
import kotlin.time.Clock

fun Route.XapiStatementsResourceRoute(
    statementResource: (ApplicationCall) -> XapiStatementsResource = { call ->
        call.requireAccountScope().get<SchoolDataSource>().xapiResource.statements
    },
    json: Json,
) {
    get(XapiStatementsResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        val responseTime = Clock.System.now()
        call.response.header(
            HttpHeaders.LastModified,
            GMTDate(responseTime.toEpochMilliseconds()).toHttpDate(),
        )
        call.response.header(
            OpenEelXapiConstants.HEADER_XAPI_VERSION,
            "1.0.3",
        )
        call.response.header(
            OpenEelXapiConstants.HEADER_XAPI_CONSISTENT_THROUGH,
            responseTime.toString(),
        )

        val statementResponse = statementResource(call).get(
            listParams = XapiStatementsResource.GetStatementParams.fromParams(
                params = call.request.queryParameters,
                json = json,
            ),
            dataLoadParams = DataLoadParams()
        )

        call.respondDataLoadState(statementResponse)
    }

    post(XapiStatementsResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val statementsJson: JsonElement = call.receive()
        val statements: List<XapiStatement> = json.decodeFromJsonElement(
            XapiSingleItemToListSerializer, statementsJson
        )

        val storeResult = statementResource(call).post(statements)
        call.respondDataLoadState(storeResult)
    }

}
