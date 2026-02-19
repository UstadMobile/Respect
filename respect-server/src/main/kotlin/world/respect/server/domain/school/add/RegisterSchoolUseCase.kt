package world.respect.server.domain.school.add

import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.ext.newUserInviteUid
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.StatusEnum
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.server.SchoolConfig
import world.respect.server.util.ext.HttpStatusException
import world.respect.shared.domain.account.invite.CreateInviteUseCase
import world.respect.shared.domain.createlink.CreateInviteLinkUseCase
import world.respect.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class RegisterSchoolUseCase : KoinComponent {

    @Serializable
    data class RegisterSchoolRequest(
        val schoolName: String,
        val schoolUrl: String,
    )

    @Serializable
    data class RegisterSchoolResponse(
        val schoolUrl: Url,
        val redirectUrl: Url
    )

    private val addSchoolUseCase: AddSchoolUseCase by inject()
    private val schoolConfig: SchoolConfig by inject()
    private val urlToCustomDeepLinkUseCase: UrlToCustomDeepLinkUseCase  by inject()

    suspend operator fun invoke(request: RegisterSchoolRequest): RegisterSchoolResponse {
        // Check if registration is enabled
        if (!schoolConfig.registration.enabled) {
            throw HttpStatusException("School registration is disabled", HttpStatusCode.Forbidden)
        }

        // Validate inputs
        if (request.schoolName.isBlank() || request.schoolUrl.isBlank()) {
            throw HttpStatusException("School name and URL are required", HttpStatusCode.BadRequest)
        }

        // Parse and validate URL
        val parsedUrl = try {
            Url(request.schoolUrl)
        } catch (e: Exception) {
            throw HttpStatusException(
                "Invalid URL format: ${request.schoolUrl}",
                HttpStatusCode.BadRequest
            )
        }

        // Extract subdomain from URL for use as dbUrl and rpId
        val schoolSubdomain = when (schoolConfig.registration.mode) {
            SchoolConfig.RegistrationConfig.RegistrationMode.SUBDOMAIN -> {
                parsedUrl.host.removeSuffix(".${schoolConfig.registration.topLevelDomain}")
            }
            else -> {
                // For any-url mode, use a sanitized version of the host as subdomain
                parsedUrl.host
            }
        }

        // Create school using AddSchoolUseCase
        addSchoolUseCase(
            listOf(
                AddSchoolUseCase.AddSchoolRequest(
                    school = SchoolDirectoryEntry(
                        name = LangMapStringValue(request.schoolName),
                        self = parsedUrl,
                        xapi = Url("${request.schoolUrl}/api/school/xapi"),
                        oneRoster = Url("${request.schoolUrl}/api/school/oneroster"),
                        respectExt = Url("${request.schoolUrl}/api/school/respect"),
                        rpId = schoolSubdomain,
                        lastModified = Clock.System.now(),
                        stored = Clock.System.now(),
                    ),
                    dbUrl = schoolSubdomain,
                    adminUsername = null,
                    adminPassword = null
                )
            )
        )

        val schoolScopeId = SchoolDirectoryEntryScopeId(parsedUrl, null)
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(schoolScopeId.scopeId)

        val createInviteUseCase: CreateInviteUseCase = schoolScope.get()
        val createInviteLinkUseCase: CreateInviteLinkUseCase = schoolScope.get()


        val inviteCode = Invite2.newRandomCode()

        val tenYearsFromNow = Clock.System.now() + (10 * 365).days
        val firstUserInviteUid = "${PersonRoleEnum.SYSTEM_ADMINISTRATOR.newUserInviteUid}:first"

        val invite = NewUserInvite(
            uid = firstUserInviteUid,
            code = inviteCode,
            role = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
            firstUser = true,
            status = StatusEnum.ACTIVE,
            lastModified = Clock.System.now(),
            stored = Clock.System.now(),
            approvalRequiredAfter = tenYearsFromNow
        )

        createInviteUseCase(invite)

        // Create the regular invite URL first
        val regularInviteUrl = createInviteLinkUseCase(inviteCode)

        // Convert to custom deep link so it opens directly in the app
        val customDeepLinkUrl = urlToCustomDeepLinkUseCase(regularInviteUrl)

        return RegisterSchoolResponse(
            schoolUrl = Url(request.schoolUrl),
            redirectUrl = customDeepLinkUrl
        )
    }

}

class SchoolRegistrationDisabledException(message: String) :
    HttpStatusException(message, HttpStatusCode.Forbidden)

class InvalidSchoolRegistrationRequestException(message: String) :
    HttpStatusException(message, HttpStatusCode.BadRequest)