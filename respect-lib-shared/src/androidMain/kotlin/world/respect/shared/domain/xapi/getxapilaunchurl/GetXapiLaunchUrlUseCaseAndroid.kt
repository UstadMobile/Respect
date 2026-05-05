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
import world.respect.libutil.ext.randomString
import world.respect.shared.domain.account.RespectAccountManager

class GetXapiLaunchUrlUseCaseAndroid(
    private val nanoHttpdApp: XapiNanoHttpdApp,
    private val schoolUrl: Url,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json,
    private val accountManager: RespectAccountManager,
): GetXapiLaunchUrlUseCase {

    override suspend fun invoke(learningUnitUrl: Url): Url {
        val activePerson = accountManager.selectedAccountAndPersonFlow.first()
            ?: throw IllegalStateException("Cannot launch when there is no active person")

        return URLBuilder(learningUnitUrl).apply {
            parameters.apply {
                set("endpoint", nanoHttpdApp.localUrlForEndpoint(schoolUrl).toString())
                set(
                    "auth",
                    "Basic ${authenticatedUser.guid}:${randomString(8)}".encodeBase64()
                )
                set("actor",
                    XapiAgent(
                        name = activePerson.person.fullName(),
                        account = XapiAccount(
                            homePage = schoolUrl.toString(),
                            name = activePerson.person.username ?: throw IllegalStateException("Launcher must have username")
                        )
                    ).let {
                        json.encodeToString(XapiAgent.serializer(), it)
                    }
                )
                set("activity_id", learningUnitUrl.toString())
            }
        }.build().also {
            Napier.i("GetXapiLaunchUrlUseCase: $it")
        }
    }
}