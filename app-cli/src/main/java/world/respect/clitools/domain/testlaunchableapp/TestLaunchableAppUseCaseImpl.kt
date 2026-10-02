package world.respect.clitools.domain.testlaunchableapp

import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppUseCase
import world.respect.lib.opds.model.OpdsFeed
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
class TestLaunchableAppUseCaseImpl: TestLaunchableAppUseCase {

    override suspend fun invoke(
        request: TestLaunchableAppUseCase.Request
    ) {

    }
}