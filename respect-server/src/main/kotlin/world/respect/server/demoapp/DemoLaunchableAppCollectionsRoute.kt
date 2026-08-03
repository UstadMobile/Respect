package org.openeel.demo.demolaunchableappserver

import com.eygraber.uri.Uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.getKoin
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumContributorStringValue
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.ext.resolve
import world.respect.server.demoapp.ext.demoAppBaseUrl
import world.respect.server.domain.school.demoapp.MakeDemoAppCollectionUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase


fun Route.DemoLaunchableAppCollectionsRoute() {
    val koin = getKoin()

    get(MakeDemoAppCollectionUseCase.DEFAULT_COLLECTION_NAME) {
        call.respond(
            koin.get<MakeDemoAppCollectionUseCase>().invoke(call.demoAppBaseUrl())
        )
    }

    get("${MakeDemoAppGradeCollectionsUseCase.GRADES_DIR_NAME}/{grade}/${MakeDemoAppGradeCollectionsUseCase.COLLECTION_FILE_NAME}") {
        val baseUrl = call.demoAppBaseUrl()
        val grade = call.parameters["grade"]!!

        call.respond(
            koin.get<MakeDemoAppGradeCollectionsUseCase>().invoke(
                baseUrl = baseUrl,
                gradeNum = grade.toInt(),
            )
        )

    }

}