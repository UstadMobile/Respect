package world.respect.server.routes.school.respect

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.http.school.DataAndChangeHistory
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.domain.GetPermissionLastModifiedUseCase
import world.respect.datalayer.school.model.Enrollment
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.receiveDataAndChangeHistory
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

fun Route.EnrollmentRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
    getPermissionLastModifiedUseCase: (ApplicationCall) -> GetPermissionLastModifiedUseCase = { call ->
        call.requireAccountScope().get()
    }
) {
    val json:Json by inject()

    get(EnrollmentDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondOffsetLimitPaging(
            params = call.request.queryParameters.offsetLimitPagingLoadParams(),
            pagingSource = schoolDataSource(call).enrollmentDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                listParams = EnrollmentDataSource.GetListParams.fromParams(call.request.queryParameters)
            ).invoke(),
            getPermissionLastModifiedUseCase = getPermissionLastModifiedUseCase(call),
        )
    }

    post(EnrollmentDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)

        val request = call.receiveDataAndChangeHistory(
            json,
            ListSerializer(Enrollment.serializer()),
            DataAndChangeHistory.serializer(Enrollment.serializer())
        )

        schoolDataSource.enrollmentDataSource.store(request.data)

        if (request.changeHistories.isNotEmpty()) {
            schoolDataSource.changeHistoryDataSource.store(request.changeHistories)
            schoolDataSource.changeHistoryDataSource.markSentToServer(request.changeHistories)
        }
    }
}