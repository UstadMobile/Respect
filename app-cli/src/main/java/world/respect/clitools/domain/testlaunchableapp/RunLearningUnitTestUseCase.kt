package world.respect.clitools.domain.testlaunchableapp

import io.ktor.http.quote
import world.respect.clitools.ext.copyResourceToFile
import world.respect.lib.opds.model.Publication
import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppUseCase
import java.io.File
import kotlin.random.Random

/**
 *
 */
class RunLearningUnitTestUseCase {

    data class RunLearningUnitTestParams(
        val publication: Publication,
        val clickSteps: List<SelectRandomPublicationUseCase.ClickStep>,
        val baseDir: File,
        val launchableAppName: String,
        val appPackageId: String = APP_PACKAGE_ID,
        val testRequest: TestLaunchableAppUseCase.Request,
        val offline: Boolean = Random.nextBoolean(),
    )

    suspend operator fun invoke(
        params: RunLearningUnitTestParams
    ) {
        val subFlowDir = params.baseDir.resolve("subflows").also {
            if(!it.exists()) {
                it.mkdirs()
            }
        }

        val mainFlowFile = File(params.baseDir, "test_launchable_app_main.yaml").also {
            this::class.java.copyResourceToFile(
                "/flows/test_launchable_app_main.yaml",it
            )
        }

        listOf("AddApp.yaml", "gotoapp.yaml", "OfflineTrue.yaml").forEach { resName ->
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
                    append("- scrollUntilVisible:\n")
                    append("    element: ${step.text.quote()}\n")
                    append("- tapOn: ${step.text.quote()}\n")
                }
            }
        )

        val cmd = listOf(
            "maestro",
            "test",
            "--env=SCHOOL_URL=${params.testRequest.serverUrl}",
            "--env=SCHOOL_ADMIN_PASSWORD=${params.testRequest.password}",
            "--env=TEST_APP_URL=${params.testRequest.manifestUrl}",
            "--env=TEST_APP_MODE=${params.testRequest.mode.id}",
            "--env=OFFLINE_STATUS=${params.offline}",
            "--env=TEST_APP_NAME=${params.launchableAppName}",
            mainFlowFile.absolutePath
        )

        println(cmd.joinToString(separator = " "))
    }

    companion object {

        const val APP_PACKAGE_ID = "world.respect.app"
    }
}