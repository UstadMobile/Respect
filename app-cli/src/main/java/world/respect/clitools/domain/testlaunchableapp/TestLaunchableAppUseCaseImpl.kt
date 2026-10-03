package world.respect.clitools.domain.testlaunchableapp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppUseCase
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findCollection
import world.respect.libutil.ext.resolve
import world.respect.shared.ext.selectPreferredString
import java.io.File

/**
 * JVM implementation for [TestLaunchableAppUseCase]. This will
 * a) Select the specified number of learning units at random by following the link from the app
 *    manifest to the app's default collection (an OpdsFeed).
 * b) For each selected learning unit:
 *    i) create a new directory that contains:
 *        The test_launchable_app_main.yaml flow and subflows from the resources
 *        A generated Maestro flow file that navigates to the given learning unit
 *   ii) Run the maestro command to run the test using ProcessBuilder
 *   iii) Wait for the process to complete.
 */
class TestLaunchableAppUseCaseImpl(
    private val selectRandomPublicationUseCase: SelectRandomPublicationUseCase,
    private val runLearningUnitTestUseCase: RunLearningUnitTestUseCase,
    private val httpClient: HttpClient,
): TestLaunchableAppUseCase {

    override suspend fun invoke(
        request: TestLaunchableAppUseCase.Request
    ) {
        val manifestPub: Publication = httpClient.get(request.manifestUrl).body()
        val defaultCollectionUrl = manifestPub.findCollection()?.let {
            request.manifestUrl.resolve(it.href)
        } ?: throw IllegalArgumentException("Manifest does not contain a default collection")
        val appName = manifestPub.metadata.title.selectPreferredString(listOf("en"))

        for(index in 0 until request.numLearningUnits) {
            val learningUnitSelection = selectRandomPublicationUseCase(
                request = SelectRandomPublicationUseCase.Request(defaultCollectionUrl)
            )

            runLearningUnitTestUseCase(
                params = RunLearningUnitTestUseCase.RunLearningUnitTestParams(
                    publication = learningUnitSelection.publication,
                    clickSteps = learningUnitSelection.clickPath,
                    baseDir = File(request.outputDir, "test_$index"),
                    launchableAppName = appName,
                    testRequest = request,
                )
            )
        }
    }

}