package world.respect.shared.domain.xapi.getxapilaunchurl

import io.github.aakira.napier.Napier
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.util.encodeBase64
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.school.ext.fullName
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp
import world.respect.libutil.ext.appendAssignmentXapiSegment
import world.respect.libutil.ext.randomString
import world.respect.shared.domain.account.RespectAccountManager

class GetXapiLaunchUrlUseCaseAndroid(
    private val nanoHttpdApp: XapiNanoHttpdApp,
    private val schoolUrl: Url,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json,
    private val accountManager: RespectAccountManager,
): GetXapiLaunchUrlUseCase {

    override suspend fun invoke(
        learningUnitUrl: Url,
        assignmentActivityId: String?,
    ): Url {
        val activeSession = accountManager.selectedAccountAndPersonFlow.first()
            ?: throw IllegalStateException("Cannot launch when there is no active person")

        return URLBuilder(learningUnitUrl).apply {
            parameters.apply {
                set(name = "endpoint",
                    value = nanoHttpdApp.localUrlForEndpoint(schoolUrl).let {
                        if(assignmentActivityId != null) {
                            it.appendAssignmentXapiSegment(assignmentActivityId)
                        }else {
                            it
                        }
                    }.toString()
                )

                val basicAuth = "${authenticatedUser.guid}:${randomString(8)}".encodeBase64()
                set(
                    "auth",
                    "Basic $basicAuth"
                )
                set(
                    name ="actor",
                    value = json.encodeToString(
                        XapiAgent.serializer(), activeSession.xapiAgent
                    )
                )
                set("activity_id", learningUnitUrl.toString())
            }
        }.build().also {
            Napier.i("GetXapiLaunchUrlUseCase: $it")
        }
    }
}