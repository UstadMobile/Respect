package world.respect.shared.domain.xapi.getxapilaunchurl

import io.github.aakira.napier.Napier
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.util.encodeBase64
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.identifierHash
import world.respect.datalayer.db.school.xapi.entities.XapiSessionEntity
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.findLearningUnitAcquisitionLinks
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp
import world.respect.libutil.ext.appendAssignmentXapiSegment
import world.respect.libutil.ext.randomString
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.opds.getxapiactivityid.GetXapiActivityForPublicationUseCase

class GetXapiLaunchUrlUseCaseAndroid(
    private val nanoHttpdApp: XapiNanoHttpdApp,
    private val schoolUrl: Url,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json,
    private val accountManager: RespectAccountManager,
    private val getXapiActivityForPublicationUseCase: GetXapiActivityForPublicationUseCase,
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
): GetXapiLaunchUrlUseCase {

    override suspend fun invoke(
        publication: OpdsPublication,
        publicationUrl: Url,
        assignmentActivityId: String?,
        useEmbeddedHttp: Boolean,
    ): Url {
        val activeSession = accountManager.selectedAccountAndPersonFlow.first()
            ?: throw IllegalStateException("Cannot launch when there is no active person")

        val learningUnitHref = publication.findLearningUnitAcquisitionLinks().firstOrNull()
            ?.href ?: throw IllegalArgumentException("Publication has no suitable acquisition link to launch")
        val learningUnitUrl = publicationUrl.resolve(learningUnitHref)

        val xapiSessionEntity = XapiSessionEntity(
            xseActorUid = activeSession.xapiAgent.identifierHash(uidNumberMapper),
            xseAccountPersonUid = authenticatedUser.guid,
            xseStartTime = System.currentTimeMillis(),
            xseAuth = randomString(10),
        )

        val xseUid = schoolDb.getXapiSessionEntityDao().insertAsync(xapiSessionEntity)

        return URLBuilder(learningUnitUrl).apply {
            val baseEndpoint = if(useEmbeddedHttp) {
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
                getXapiActivityForPublicationUseCase(publication).id
            )
            encodedParameters["auth"] = UrlEncoderUtil.encode("Basic $basicAuth")
            encodedParameters["actor"] =  UrlEncoderUtil.encode(
                json.encodeToString(
                    XapiAgent.serializer(), activeSession.xapiAgent,
                )
            )
        }.build().also {
            Napier.i("GetXapiLaunchUrlUseCase: $it")
        }
    }
}