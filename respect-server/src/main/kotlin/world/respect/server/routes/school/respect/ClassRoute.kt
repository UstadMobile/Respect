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
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Clazz
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.receiveDataAndChangeHistory
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

@Suppress("FunctionName")
fun Route.ClassRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    val json:Json by inject()

    get(ClassDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondOffsetLimitPaging(
            params = call.request.queryParameters.offsetLimitPagingLoadParams(),
            pagingSource = schoolDataSource(call).classDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = ClassDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            ).invoke()
        )
    }

    post(ClassDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)

        val request = call.receiveDataAndChangeHistory(
            json,
            ListSerializer(Clazz.serializer()),
            DataAndChangeHistory.serializer(Clazz.serializer())
        )

        schoolDataSource.classDataSource.store(request.data)

        if (request.changeHistories.isNotEmpty()) {
            schoolDataSource.changeHistoryDataSource.store(request.changeHistories)
            schoolDataSource.changeHistoryDataSource.markSentToServer(request.changeHistories)
        }
    }

}