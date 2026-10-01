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
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondDataLoadState

@Suppress("FunctionName")
fun Route.SchoolConfigSettingRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(SchoolConfigSettingDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondDataLoadState(
            schoolDataSource(call).schoolConfigSettingDataSource.list(
                loadParams = DataLoadParams(),
                params = SchoolConfigSettingDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            )
        )
    }

    post(SchoolConfigSettingDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        val settings: List<SchoolConfigSetting> = call.receive()
        schoolDataSource.schoolConfigSettingDataSource.store(settings)
        call.respond(HttpStatusCode.NoContent)
    }
}
