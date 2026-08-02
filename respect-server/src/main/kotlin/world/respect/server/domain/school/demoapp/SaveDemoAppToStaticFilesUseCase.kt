package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase.Companion.APP_MANIFEST_FILENAME
import java.io.File

class SaveDemoAppToStaticFilesUseCase(
    private val makeDemoAppManifestUseCase: MakeDemoAppManifestUseCase,
    private val json: Json,
) {

    operator fun invoke(
        destDir: File,
        baseUrl: Url,
    ) {
        destDir.takeIf { !it.exists() }?.mkdirs()

        File(destDir, APP_MANIFEST_FILENAME).writeText(
            json.encodeToString(makeDemoAppManifestUseCase(baseUrl))
        )

        File(destDir, MakeDemoAppManifestUseCase.APP_MANIFEST_ICON_NAME).writeBytes(
            this::class.java.getResourceAsStream("/demoapp/app_icon.png")!!.readBytes()
        )

    }
}