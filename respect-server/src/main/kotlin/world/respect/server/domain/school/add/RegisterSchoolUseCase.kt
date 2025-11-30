package world.respect.server.domain.school.add

import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.server.SchoolConfig
import world.respect.server.util.ext.HttpStatusException
import kotlin.time.Clock

class RegisterSchoolUseCase : KoinComponent {

    @Serializable
    data class RegisterSchoolRequest(
        val schoolName: String,
        val schoolUrl: String,
        val redirectUrl: String = "world.respect.app://school-registered"
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
                parsedUrl.host.replace("[^a-zA-Z0-9]".toRegex(), "-").lowercase()
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

        return RegisterSchoolResponse(
            schoolUrl = request.schoolUrl,
            redirectUrl = request.redirectUrl
        )
    }
}

class SchoolRegistrationDisabledException(message: String) :
    HttpStatusException(message, HttpStatusCode.Forbidden)

class InvalidSchoolRegistrationRequestException(message: String) :
    HttpStatusException(message, HttpStatusCode.BadRequest)