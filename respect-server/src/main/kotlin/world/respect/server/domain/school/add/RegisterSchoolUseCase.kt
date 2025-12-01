package world.respect.server.domain.school.add

import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.server.SchoolConfig
import world.respect.server.util.ext.HttpStatusException
import world.respect.shared.domain.account.invite.CreateInviteUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import java.util.UUID
import kotlin.time.Clock

class RegisterSchoolUseCase : KoinComponent {

    @Serializable
    data class RegisterSchoolRequest(
        val schoolName: String,
        val schoolUrl: String,
    )

    @Serializable
    data class RegisterSchoolResponse(
        val schoolUrl: String,
        val redirectUrl: String
    )

    private val addSchoolUseCase: AddSchoolUseCase by inject()
    private val schoolConfig: SchoolConfig by inject()

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

        // Create an invite for the new school admin
        val invite = Invite(
            guid = UUID.randomUUID().toString(),
            code = generateInviteCode(),
            newRole = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
            forClassGuid = null,
            forFamilyOfGuid = null,
            forClassRole = null,
            inviteMultipleAllowed = false,
            approvalRequired = false,
            expiration = Clock.System.now().toEpochMilliseconds() + Invite.EXPIRATION_TIME,
            lastModified = Clock.System.now(),
            stored = Clock.System.now(),
            inviteStatus = Invite.STATUS_PENDING
        )
        val properInviteUrl = createInviteUseCase(invite)

        return RegisterSchoolResponse(
            schoolUrl = request.schoolUrl,
            redirectUrl = properInviteUrl
        )
    }

    private fun generateInviteCode(): String {
        // Generate a random 8-character invite code
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
}

class SchoolRegistrationDisabledException(message: String) :
    HttpStatusException(message, HttpStatusCode.Forbidden)

class InvalidSchoolRegistrationRequestException(message: String) :
    HttpStatusException(message, HttpStatusCode.BadRequest)