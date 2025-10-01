package world.respect.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
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
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.server.domain.school.add.AddDirectoryUseCase
import world.respect.server.domain.school.add.AddSchoolUseCase
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
            val addSchoolRequests: List<AddSchoolUseCase.AddSchoolRequest> = call.receive()
            addSchoolUseCase(addSchoolRequests)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    post("addirectory") {
        val addDirectoryUseCase: AddDirectoryUseCase by inject()
        val url = call.request.queryParameters["url"]
            ?: throw IllegalArgumentException("missing url param").withHttpStatus(400)
        addDirectoryUseCase(Url(url))
        call.respond(HttpStatusCode.NoContent)
    }
}