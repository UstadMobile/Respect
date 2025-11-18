package world.respect.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.server.domain.school.add.AddSchoolUseCase
import world.respect.server.domain.school.add.InvalidSchoolDomainException
import world.respect.server.domain.school.add.SchoolRegistrationDisabledException
import world.respect.server.util.ext.respondDataLoadState

const val AUTH_CONFIG_DIRECTORY_ADMIN_BASIC = "auth-directory-admin-basic"

fun Route.RespectSchoolDirectoryRoute(
    respectAppDataSource: RespectAppDataSource,
) {
    get("school") {
        call.respondDataLoadState(
            respectAppDataSource.schoolDirectoryEntryDataSource.list(
                loadParams = DataLoadParams(),
                listParams = SchoolDirectoryEntryDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            )
        )
    }

    authenticate(AUTH_CONFIG_DIRECTORY_ADMIN_BASIC) {
        post("school") {
            val addSchoolUseCase: AddSchoolUseCase by inject()

            try {
                val addSchoolRequests: List<AddSchoolUseCase.AddSchoolRequest> = call.receive()
                addSchoolUseCase(addSchoolRequests)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: SchoolRegistrationDisabledException) {
                call.respond(HttpStatusCode.Forbidden, "School registration is disabled")
            } catch (e: InvalidSchoolDomainException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid school domain")
            }
        }
    }
}