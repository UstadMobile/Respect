package world.respect.server.routes.school.xapi

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.server.util.ext.requireAccountScope
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState
import kotlin.time.Clock

fun Route.XapiActivityProfileRoute(
    resource: (ApplicationCall) -> XapiActivityProfileResource = { call ->
        call.requireAccountScope().get<SchoolDataSource>().xapiResource.activityProfile
    },
)  {
    get(XapiActivityProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val profileId = call.request.queryParameters["profileId"]
        if (profileId != null) {
            val singleParams = XapiActivityProfileResource.SingleDocumentParams.fromParams(
                params = call.request.queryParameters
            )
            val docResponse = resource(call).get(
                params = singleParams,
                dataLoadParams = DataLoadParams(),
            )
            call.respondDataLoadState(
                dataLoadState = docResponse,
                onRespondWithData = { doc ->
                    respondBytes(
                        bytes = doc.contentsAsByteArray(),
                        contentType = ContentType.parse(doc.type)
                    )
                }
            )
        } else {
            val multiParams = XapiActivityProfileResource.MultiDocParams.fromParams(
                params = call.request.queryParameters
            )
            val listResponse = resource(call).getMultipleDocuments(
                params = multiParams,
                dataLoadParams = DataLoadParams(),
            )
            call.respondDataLoadState(
                dataLoadState = listResponse,
            )
        }
    }

    post(XapiActivityProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val singleParams = XapiActivityProfileResource.SingleDocumentParams.fromParams(
            params = call.request.queryParameters
        )
        val contentType = call.request.headers[HttpHeaders.ContentType] ?: "application/json"
        val bytes = call.receive<ByteArray>()
        val document = XapiDocumentByteArrayImpl(
            type = contentType,
            updated = Clock.System.now(),
            contents = bytes,
        )

        resource(call).post(
            params = singleParams,
            document = document,
        )
        call.respond(HttpStatusCode.NoContent)
    }

    put(XapiActivityProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val singleParams = XapiActivityProfileResource.SingleDocumentParams.fromParams(
            params = call.request.queryParameters
        )
        val contentType = call.request.headers[HttpHeaders.ContentType] ?: "application/json"
        val bytes = call.receive<ByteArray>()
        val document = XapiDocumentByteArrayImpl(
            type = contentType,
            updated = Clock.System.now(),
            contents = bytes,
        )

        resource(call).put(
            params = singleParams,
            document = document,
        )
        call.respond(HttpStatusCode.NoContent)
    }

    delete(XapiActivityProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val singleParams = XapiActivityProfileResource.SingleDocumentParams.fromParams(
            params = call.request.queryParameters
        )

        resource(call).delete(
            params = singleParams,
        )
        call.respond(HttpStatusCode.NoContent)
    }
}