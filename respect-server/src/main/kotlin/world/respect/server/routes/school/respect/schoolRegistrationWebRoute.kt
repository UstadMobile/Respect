package world.respect.server.routes.school.respect

import io.ktor.server.html.respondHtml
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.onInput
import kotlinx.html.p
import kotlinx.html.title
import org.koin.ktor.ext.inject
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.server.SchoolConfig
import world.respect.server.domain.school.add.AddSchoolUseCase

fun Route.schoolRegistrationWebRoute() {
    val addSchoolUseCase: AddSchoolUseCase by inject()
    val schoolConfig: SchoolConfig by inject()

    // Show registration form
    get("/register-school") {
        // Check if registration is enabled
        if (!schoolConfig.registration.enabled) {
            call.respondText("School registration is disabled", status = io.ktor.http.HttpStatusCode.Forbidden)
            return@get
        }

        call.respondHtml {
            head {
                title { +"Register New School" }
            }
            body {
                h1 { +"Register New School" }

                form(method = FormMethod.post, action = "/register-school") {
                    div("form-group") {
                        label {
                            htmlFor = "schoolName"
                            +"School Name"
                        }
                        input(type = InputType.text, name = "schoolName") {
                            id = "schoolName"
                            required = true
                            placeholder = "Enter school name"
                        }
                    }

                    when (schoolConfig.registration.mode) {
                        SchoolConfig.RegistrationConfig.RegistrationMode.SUBDOMAIN -> {
                            div("form-group") {
                                label {
                                    htmlFor = "schoolSubdomain"
                                    +"School Link"
                                }
                                input(type = InputType.text, name = "schoolSubdomain") {
                                    id = "schoolSubdomain"
                                    required = true
                                    placeholder = "school-name"
                                    onInput = "updateFullUrl()"
                                }
                            }
                            // Hidden field for full URL
                            input(type = InputType.hidden, name = "schoolUrl") {
                                id = "schoolUrl"
                                value = "https://school-name.${schoolConfig.registration.topLevelDomain}"
                            }
                        }
                        SchoolConfig.RegistrationConfig.RegistrationMode.ANY_URL -> {
                            div("form-group") {
                                label {
                                    htmlFor = "schoolUrl"
                                    +"School URL"
                                }
                                input(type = InputType.text, name = "schoolUrl") {
                                    id = "schoolUrl"
                                    required = true
                                    placeholder = "https://your-school-domain.org"
                                }
                                div("domain-hint") {
                                    +"Enter the full URL where your school will be hosted"
                                }
                            }
                        }
                        else -> {
                            // Should not reach here since registration is disabled
                        }
                    }

                    // Hidden redirect field for deep linking to app
                    input(type = InputType.hidden, name = "redirect") {
                        value = call.request.queryParameters["redirect"] ?: "world.respect.app://school-registered"
                    }

                    button(type = ButtonType.submit, classes = "submit-btn") {
                        +"Next"
                    }
                }
            }
        }
    }

// Handle form submission
    post("/register-school") {
        // Check if registration is enabled
        if (!schoolConfig.registration.enabled) {
            call.respondText("School registration is disabled", status = io.ktor.http.HttpStatusCode.Forbidden)
            return@post
        }

        val parameters = call.receiveParameters()
        val schoolName = parameters["schoolName"] ?: ""
        val schoolUrl = parameters["schoolUrl"] ?: ""
        val redirectUrl = parameters["redirect"] ?: "world.respect.app://school-registered"

        try {
            // Validate inputs
            if (schoolName.isBlank() || schoolUrl.isBlank()) {
                call.respondText("School name and URL are required", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@post
            }

            // Parse and validate URL
            val parsedUrl = try {
                io.ktor.http.Url(schoolUrl)
            } catch (e: Exception) {
                call.respondText("Invalid URL format", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@post
            }

            // For subdomain mode, validate the domain matches the configured top-level domain
            if (schoolConfig.registration.mode == SchoolConfig.RegistrationConfig.RegistrationMode.SUBDOMAIN) {
                if (!parsedUrl.host.endsWith(".${schoolConfig.registration.topLevelDomain}")) {
                    call.respondText(
                        "School URL must be a subdomain of ${schoolConfig.registration.topLevelDomain}",
                        status = io.ktor.http.HttpStatusCode.BadRequest
                    )
                    return@post
                }
            }

            // Extract subdomain from URL for use as dbUrl and rpId
            val schoolSubdomain = if (schoolConfig.registration.mode == SchoolConfig.RegistrationConfig.RegistrationMode.SUBDOMAIN) {
                parsedUrl.host.removeSuffix(".${schoolConfig.registration.topLevelDomain}")
            } else {
                // For any-url mode, use a sanitized version of the host as subdomain
                parsedUrl.host.replace("[^a-zA-Z0-9]".toRegex(), "-").lowercase()
            }

            // Create school using AddSchoolUseCase
            addSchoolUseCase(
                listOf(
                    AddSchoolUseCase.AddSchoolRequest(
                        school = SchoolDirectoryEntry(
                            name = LangMapStringValue(schoolName),
                            self = parsedUrl,
                            xapi = io.ktor.http.Url("$schoolUrl/api/school/xapi"),
                            oneRoster = io.ktor.http.Url("$schoolUrl/api/school/oneroster"),
                            respectExt = io.ktor.http.Url("$schoolUrl/api/school/respect"),
                            rpId = schoolSubdomain,
                            lastModified = kotlin.time.Clock.System.now(),
                            stored = kotlin.time.Clock.System.now(),
                        ),
                        dbUrl = schoolSubdomain,
                        adminUsername = "admin",
                        adminPassword = "changeme123"
                    )
                )
            )

            val redirectWithParams = if (redirectUrl.contains("?")) {
                "$redirectUrl&schoolUrl=${java.net.URLEncoder.encode(schoolUrl, "UTF-8")}"
            } else {
                "$redirectUrl?schoolUrl=${java.net.URLEncoder.encode(schoolUrl, "UTF-8")}"
            }

            println("DEBUG: Redirecting to: $redirectWithParams")

            call.respondRedirect(redirectWithParams)

        } catch (e: Exception) {
            call.respondHtml {
                head {
                    title { +"Registration Error" }
                }
                body {
                    h1 { +"Registration Failed" }
                    p { +"Error: ${e.message}" }
                    a(href = "/register-school") { +"Try Again" }
                }
            }
        }
    }
}