package world.respect.server.routes.school.respect

import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.lib.opds.model.OpdsFeed
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondDataLoadState
import world.respect.server.util.ext.virtualHost

fun Route.PlaylistRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    val playlistEndpointPath = "${OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME}/{uuid}"

    fun ApplicationCall.playlistUrl(): Url {
        //This is safe because otherwise the path would not have matched
        val playlistUuid = parameters["uuid"]!!
        return virtualHost.appendEndpointSegments(
            OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME, playlistUuid
        )
    }

    get(playlistEndpointPath) {
        val schoolDataSource = schoolDataSource(call)

        call.respondDataLoadState(
            schoolDataSource.opdsFeedDataSource.getByUrl(
                url = call.playlistUrl(),
                params = DataLoadParams()
            )
        )
    }


    post(playlistEndpointPath) {
        val schoolDataSource = schoolDataSource(call)

        val opdsFeed: OpdsFeed = call.receive()
        schoolDataSource.opdsFeedDataSource.store(listOf(opdsFeed))
        call.respond(HttpStatusCode.NoContent)
    }
}