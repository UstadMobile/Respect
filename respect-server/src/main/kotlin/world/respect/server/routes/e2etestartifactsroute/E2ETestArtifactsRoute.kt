package world.respect.server.routes.e2etestartifactsroute

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.shared.domain.e2eartifactupload.E2EArtifactUploadUseCase
import java.io.File
import java.sql.DriverManager
import world.respect.shared.domain.school.SchoolDbPath


@Suppress("FunctionName")
fun Route.ReceiveE2EArtifactUploadRoute(e2eUploadsDir: File) {
    e2eUploadsDir.mkdirs()
    post(E2EArtifactUploadUseCase.ENDPOINT_RECEIVE) {
        val name = requireNotNull(call.request.queryParameters["name"])
        if (!name.all { it.isLetterOrDigit() || it == '_' }) {
            call.respond(HttpStatusCode.BadRequest, "name must contain only letters, digits, or underscores")
            return@post
        }
        val tempFile = File(e2eUploadsDir, "$name.tmp")
        tempFile.writeBytes(call.receive<ByteArray>())

        if (!tempFile.isValidSQLiteDb()) {
            tempFile.delete()
            call.respond(HttpStatusCode.BadRequest, "corrupt SQLite database")
            return@post
        }

        tempFile.renameTo(File(e2eUploadsDir, "$name${SchoolDbPath.DB_EXTENSION}"))
        call.respond(HttpStatusCode.OK)
    }
}

private fun File.isValidSQLiteDb(): Boolean = try {
    DriverManager.getConnection("jdbc:sqlite:$absolutePath").use { conn ->
        conn.createStatement().use { it.execute("PRAGMA integrity_check") }
    }
    true
} catch (_: Exception) {
    false
}