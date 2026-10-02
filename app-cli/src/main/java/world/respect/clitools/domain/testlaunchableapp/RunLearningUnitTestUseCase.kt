package world.respect.clitools.domain.testlaunchableapp

import io.ktor.http.quote
import world.respect.clitools.ext.copyResourceToFile
import world.respect.lib.opds.model.Publication
import java.io.File

/**
 *
 */
class RunLearningUnitTestUseCase {

    data class RunLearningUnitTestParams(
        val publication: Publication,
        val clickSteps: List<SelectRandomPublicationUseCase.ClickStep>,
        val baseDir: File,
        val appPackageId: String = APP_PACKAGE_ID,
    )

    suspend operator fun invoke(
        params: RunLearningUnitTestParams
    ) {
        val subFlowDir = params.baseDir.resolve("subflows").also {
            if(!it.exists()) {
                it.mkdirs()
            }
        }

        this::class.java.copyResourceToFile(
            "/flows/test_launchable_app_main.yaml",
            File(params.baseDir, "test_launchable_app_main.yaml")
        )

        listOf("AddApp.yaml", "OfflineTrue.yaml").forEach { resName ->
            this::class.java.copyResourceToFile(
                "/flows/subflows/$resName",
                File(subFlowDir, resName)
            )
        }

        File(params.baseDir, "generated/gotolearningunit.yaml").also {
            if(!it.parentFile.exists())
                it.parentFile.mkdirs()
        }.writeText(
            buildString {
                append("appId: world.respect.app\n")
                append("---\n")
                params.clickSteps.forEach { step ->
                    append("- tapOn: ${step.text.quote()}\n")
                }
            }
        )
    }

    companion object {

        const val APP_PACKAGE_ID = "world.respect.app"
    }
}