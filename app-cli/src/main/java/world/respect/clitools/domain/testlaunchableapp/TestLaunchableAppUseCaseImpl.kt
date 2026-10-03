package world.respect.clitools.domain.testlaunchableapp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.http.school.xapi.XapiStatementsResourceHttpClient
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findCollection
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.model.XapiVerb
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCaseClient
import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppUseCase
import world.respect.shared.domain.validator.ValidatorMessage
import world.respect.shared.ext.selectPreferredString
import java.io.File
import kotlin.time.Clock

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
    private val getXapiStatementsFromLearningUnitTestUseCase: GetXapiStatementsFromLearningUnitTestUseCase,
    private val httpClient: HttpClient,
    private val json: Json,
): TestLaunchableAppUseCase {

    override suspend fun invoke(
        request: TestLaunchableAppUseCase.Request
    ): TestLaunchableAppUseCase.Result {
        val messages = mutableListOf<ValidatorMessage>()

        val manifestPub: Publication = httpClient.get(request.manifestUrl).body()
        val defaultCollectionUrl = manifestPub.findCollection()?.let {
            request.manifestUrl.resolve(it.href)
        } ?: throw IllegalArgumentException("Manifest does not contain a default collection")
        val appName = manifestPub.metadata.title.selectPreferredString(listOf("en"))

        val authResponse = GetTokenAndUserProfileWithCredentialUseCaseClient(
            schoolUrl = request.serverUrl,
            httpClient = httpClient,
            getDeviceInfoUseCase = null,
        ).invoke(
            credential = RespectPasswordCredential(request.username, request.password)
        )

        val statementResource = XapiStatementsResourceHttpClient(
            httpClient = httpClient,
            xapiUrl = {
                request.serverUrl.appendEndpointSegments("api/school/xapi")
            },
            tokenProvider = { authResponse.token },
            json = json
        )


        for(index in 0 until request.numLearningUnits) {
            val testStartTime = Clock.System.now()
            val learningUnitSelection = selectRandomPublicationUseCase(
                request = SelectRandomPublicationUseCase.Request(defaultCollectionUrl)
            )

            val learningUnitOutputDir = File(request.outputDir, "test_$index")

            runLearningUnitTestUseCase(
                params = RunLearningUnitTestUseCase.RunLearningUnitTestParams(
                    publication = learningUnitSelection.publication,
                    clickSteps = learningUnitSelection.clickPath,
                    baseDir = learningUnitOutputDir,
                    launchableAppName = appName,
                    testRequest = request,
                )
            ).also {
                messages.addAll(it.messages)
            }

            val publicationUrl = Url(learningUnitSelection.clickPath.last().link.href)
            val statements = getXapiStatementsFromLearningUnitTestUseCase(
                publicationUrl = publicationUrl,
                testStartTime = testStartTime,
                statementResource = statementResource,
            )

            println("Found ${statements.statements.size} statements")
            File(learningUnitOutputDir, "statements.json").writeText(
                json.encodeToString(
                    XapiStatementResult.serializer(),
                    statements,
                )
            )

            if(
                !statements.statements.any {
                    it.verb.id == XapiVerb.ID_COMPLETED || it.verb.id == XapiVerb.ID_PASSED
                            || it.verb.id == XapiVerb.ID_FAILED
                }
            ) {
                messages.add(
                    ValidatorMessage(
                        sourceUri = publicationUrl.toString(),
                        message = "No complete, passed, or failed Xapi Statement after testing $publicationUrl"
                    ).also {
                        println(it.toString())
                    }
                )
            }
        }

        return TestLaunchableAppUseCase.Result(
            messages = messages.toList()
        )
    }
}