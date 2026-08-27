package world.respect.shared.domain.xapi.getxapilaunchurl

import android.content.Context
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.util.encodeBase64
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import nl.adaptivity.xmlutil.serialization.XML
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.identifierHash
import world.respect.datalayer.db.school.xapi.entities.XapiSessionEntity
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findLearningUnitAcquisitionLinks
import world.respect.lib.opds.model.findTinCanXmlLink
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.libutil.ext.appendAssignmentXapiSegment
import world.respect.libutil.ext.randomString
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.opds.getxapiactivityid.GetXapiActivityForPublicationUseCase
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent

class GetXapiLaunchUrlUseCaseAndroid(
    private val nanoHttpdApp: XapiNanoHttpdApp,
    private val schoolUrl: Url,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json,
    private val accountManager: RespectAccountManager,
    private val getXapiActivityForPublicationUseCase: GetXapiActivityForPublicationUseCase,
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val applicationContext: Context,
    private val httpClient: HttpClient,
    private val xml: XML,
): GetXapiLaunchUrlUseCase {

    override suspend fun invoke(
        publication: Publication,
        publicationUrl: Url,
        assignmentActivityId: String?,
        type: GetXapiLaunchUrlUseCase.LaunchType,
    ): Url {
        val activeSession = accountManager.selectedAccountAndPersonFlow.first()
            ?: throw IllegalStateException("Cannot launch when there is no active person")

        val tinCanLink = publication.findTinCanXmlLink()
        val tinCanXmlUrl = tinCanLink?.let { publicationUrl.resolve(it.href) }
        val tinCanXmlActivity = tinCanXmlUrl?.let {
            val tinCanXmlContent = httpClient.get(tinCanXmlUrl).bodyAsText()

            xml.decodeFromString(
                TinCanXmlDocument.serializer(), tinCanXmlContent
            ).activities.activity.firstOrNull()
        }

        val tinCanLaunchUrl = tinCanXmlActivity?.launch?.value?.let { tinCanXmlUrl.resolve(it) }

        val legacyUrl = if(tinCanLaunchUrl == null) {
            publication.findLearningUnitAcquisitionLinks().firstOrNull()
                ?.href?.let { publicationUrl.resolve(it) }
                ?: throw IllegalArgumentException("Publication has no suitable acquisition link to launch")
        }else {
            null
        }

        val xapiSessionEntity = XapiSessionEntity(
            xseActorUid = activeSession.xapiAgent.identifierHash(uidNumberMapper),
            xseAccountPersonUid = authenticatedUser.guid,
            xseStartTime = System.currentTimeMillis(),
            xseAuth = randomString(10),
        )

        val xseUid = schoolDb.getXapiSessionEntityDao().insertAsync(xapiSessionEntity)

        val learningUnitUrl = tinCanLaunchUrl ?: legacyUrl ?: throw IllegalStateException()
        return URLBuilder(learningUnitUrl).apply {
            val baseEndpoint = if(type == GetXapiLaunchUrlUseCase.LaunchType.WEBVIEW) {
                nanoHttpdApp.localUrlForEndpoint(schoolUrl)
            }else {
                schoolUrl
            }
            val basicAuth = "${xseUid}:${xapiSessionEntity.xseAuth}".encodeBase64()

            /*
             * Using the normal parameters with the default encoding results in values that include
             * the + instead of space, which is not correctly decoded by Jetpack compose navigation
             * deep link.
             */
            encodedParameters["endpoint"] = UrlEncoderUtil.encode(
                baseEndpoint.let {
                    if(assignmentActivityId != null) {
                        it.appendAssignmentXapiSegment(assignmentActivityId)
                    }else {
                        it
                    }
                }.toString()
            )

            encodedParameters["activity_id"] = UrlEncoderUtil.encode(
                tinCanXmlActivity?.id ?: getXapiActivityForPublicationUseCase(publication).id
            )
            encodedParameters["auth"] = UrlEncoderUtil.encode("Basic $basicAuth")
            encodedParameters["actor"] =  UrlEncoderUtil.encode(
                json.encodeToString(
                    XapiAgent.serializer(), activeSession.xapiAgent,
                )
            )

            if(type == GetXapiLaunchUrlUseCase.LaunchType.NATIVE) {
                parameters[XapiIpcIntent.PARAM_NAME_IPC_SERVICE_PACKAGE] = applicationContext.packageName
            }
        }.build().also {
            Napier.i("GetXapiLaunchUrlUseCase: $it")
        }
    }
}