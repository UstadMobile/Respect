package world.respect.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.flow.first
import org.koin.ktor.ext.inject
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.server.domain.school.add.AddSchoolUseCase

const val AUTH_CONFIG_DIRECTORY_ADMIN_BASIC = "auth-directory-admin-basic"

fun Route.RespectSchoolDirectoryRoute() {

    get("school") {
        val directoryDataSource: SchoolDirectoryDataSourceLocal by inject()
        val query = call.request.queryParameters["name"]
        val schoolsFound = directoryDataSource.searchSchools(query ?: "").first()
        when (schoolsFound) {
            is DataReadyState -> call.respond(schoolsFound.data)
            else -> call.respond(emptyList<SchoolDirectoryEntry>())
        }
    }

    authenticate(AUTH_CONFIG_DIRECTORY_ADMIN_BASIC) {
        post("school") {
            val addSchoolUseCase: AddSchoolUseCase by inject()

            val addSchoolRequests: List<AddSchoolUseCase.AddSchoolRequest> = call.receive()
            addSchoolUseCase(addSchoolRequests)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    post("invite") {

    }
    get("invite"){
        val code = call.request.queryParameters["code"]

        if (code.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing 'code' parameter")
            return@get
        }

        val directoryDataSource: SchoolDirectoryDataSourceLocal by inject()
        val inviteInfo = directoryDataSource.getInviteInfo(code)

        call.respond(inviteInfo)
    }
    get("url") {
        val directoryDataSource: SchoolDirectoryDataSourceLocal by inject()
        val url = call.request.queryParameters["url"]

        if (url.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing 'url' parameter")
            return@get
        }
        val school = directoryDataSource.getSchoolDirectoryEntryByUrl(Url(url))

        call.respond(school)
    }

}