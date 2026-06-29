package world.respect.server.routes.e2e

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import java.io.File


@Suppress("FunctionName")
fun Route.ReceiveDbRoute(e2eUploadsDir: File) {
    e2eUploadsDir.mkdirs()
    post("receivedb") {
        val filename = File(requireNotNull(call.request.queryParameters["filename"])).name
        val dbBytes = call.receive<ByteArray>()
        File(e2eUploadsDir, filename).writeBytes(dbBytes)
        call.respond(HttpStatusCode.OK)
    }
}