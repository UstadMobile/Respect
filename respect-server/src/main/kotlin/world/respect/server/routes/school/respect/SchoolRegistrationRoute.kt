package world.respect.server.routes.school.respect

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.title
import org.koin.ktor.ext.inject
import world.respect.server.SchoolConfig
import world.respect.server.domain.school.add.RegisterSchoolUseCase
import world.respect.server.util.ext.getStatusCode
import java.net.URLEncoder
import kotlin.time.Duration.Companion.seconds

fun Route.SchoolRegistrationRoute() {
    val registerSchoolUseCase: RegisterSchoolUseCase by inject()
    val schoolConfig: SchoolConfig by inject()
    val httpClient = HttpClient()

    // Show registration form
    get("/register-school") {

        if (!schoolConfig.registration.enabled) {
            call.respondText(
                "School registration is disabled",
                status = HttpStatusCode.Forbidden
            )
            return@get
        }

        val packageName = call.request.queryParameters["packageName"]

        call.respondHtml {
            head {
                title { +"Register New School" }
                meta(name = "viewport", content = "width=device-width, initial-scale=1")
            }
            body {
                main(classes = "container") { // "container" centers the form and gives it padding
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
                                    div {
                                        input(type = InputType.text, name = "schoolSubdomain") {
                                            id = "schoolSubdomain"
                                            required = true
                                            placeholder = "schoolSubdomain"
                                        }
                                        // Show the suffix to the user
                                        +".${schoolConfig.registration.topLevelDomain}"
                                    }
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
                                        placeholder = "https://your-school.com"
                                    }
                                }
                            }

                            else -> {
                                // Should not reach here since registration is disabled
                            }
                        }

                        // Hidden package name field
                        packageName?.let { pkg ->
                            input(type = InputType.hidden, name = "packageName") {
                                value = pkg
                            }
                        }

                        // Hidden redirect field for deep linking to app
                        input(type = InputType.hidden, name = "redirect") {
                            value = call.request.queryParameters["redirect"]
                                ?: "world.respect.app://school-registered"
                        }

                        button(type = ButtonType.submit, classes = "submit-btn") {
                            +"Next"
                        }
                    }
                }
            }
        }
    }

    post("/register-school") {
        val parameters = call.receiveParameters()

        val schoolName = parameters["schoolName"] ?: ""
        val packageName = parameters["packageName"]

        try {
            val schoolUrl = when (schoolConfig.registration.mode) {
                SchoolConfig.RegistrationConfig.RegistrationMode.SUBDOMAIN -> {
                    val schoolSubdomain = parameters["schoolSubdomain"] ?: ""
                    if (schoolSubdomain.isBlank()) {
                        call.respondText(
                            "School subdomain is required",
                            status = HttpStatusCode.BadRequest
                        )
                        return@post
                    }

                    // Validate subdomain format
                    if (!isValidSubdomain(schoolSubdomain)) {
                        call.respondText(
                            "Invalid subdomain. Use only letters, numbers, and hyphens.",
                            status = HttpStatusCode.BadRequest
                        )
                        return@post
                    }

                    // Construct the URL on the server side
                    val fullUrl =
                        "https://$schoolSubdomain.${schoolConfig.registration.topLevelDomain}"

                    try {
                        val isDomainAccessible =
                            validateDomainAccessibility(schoolConfig.registration.topLevelDomain)
                        if (!isDomainAccessible) {
                            throw DomainNotAccessibleException("The main domain is not accessible")
                        }
                    } catch (e: DomainNotAccessibleException) {
                        call.respondHtml(HttpStatusCode.ServiceUnavailable) {
                            head {
                                title { +"Service Unavailable" }
                                meta(
                                    name = "viewport",
                                    content = "width=device-width, initial-scale=1"
                                )
                            }
                            body {
                                main(classes = "container") { // "container" centers the form and gives it padding
                                    h1 { +"Something Went Wrong" }
                                    div {
                                        style =
                                            "color: red; padding: 20px; background-color: #ffe6e6; border-radius: 5px;"
                                        +"We're unable to register new schools at the moment. "
                                        +"The required service (${schoolConfig.registration.topLevelDomain}) is not accessible."
                                    }
                                    p { +"Please try again later or contact support if the problem persists." }

                                    a(
                                        href = "/register-school?packageName=${
                                            packageName?.let {
                                                URLEncoder.encode(
                                                    it,
                                                    "UTF-8"
                                                )
                                            } ?: ""
                                        }") {
                                        +"Try Again"
                                    }
                                }
                            }
                        }
                        return@post
                    }

                    fullUrl
                }

                SchoolConfig.RegistrationConfig.RegistrationMode.ANY_URL -> {
                    val schoolUrlParam = parameters["schoolUrl"] ?: ""
                    if (schoolUrlParam.isBlank()) {
                        call.respondText(
                            "School URL is required",
                            status = HttpStatusCode.BadRequest
                        )
                        return@post
                    }

                    schoolUrlParam
                }

                else -> {
                    call.respondText(
                        "Registration mode not supported",
                        status = HttpStatusCode.BadRequest
                    )
                    return@post
                }
            }

            if (schoolUrl.isBlank()) {
                call.respondText(
                    "School URL is required",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            if (schoolName.isBlank()) {
                call.respondText(
                    "School name is required",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            val response = registerSchoolUseCase(
                RegisterSchoolUseCase.RegisterSchoolRequest(
                    schoolName = schoolName,
                    schoolUrl = schoolUrl,
                )
            )

            call.respondRedirect(response.redirectUrl)

        } catch (e: Exception) {
            val statusCode = e.getStatusCode() ?: HttpStatusCode.InternalServerError

            val errorMessage = if (e.message?.contains("domain is not accessible") == true ||
                e.message?.contains("Service Unavailable") == true
            ) {
                """
                Registration failed because the main domain (${schoolConfig.registration.topLevelDomain}) is not accessible.
            
                """.trimIndent()
            } else {
                "Error: ${e.message}"
            }

            call.respondHtml(statusCode) {
                head {
                    title { +"Registration Error" }
                    meta(
                        name = "viewport",
                        content = "width=device-width, initial-scale=1"
                    )
                }
                body {
                    main(classes = "container") { // "container" centers the form and gives it padding

                        h1 { +"Registration Failed" }
                        div {
                            style =
                                "color: red; padding: 20px; background-color: #ffe6e6; border-radius: 5px;"
                            +errorMessage
                        }
                        a(
                            href = "/register-school?packageName=${
                                packageName?.let {
                                    URLEncoder.encode(
                                        it,
                                        "UTF-8"
                                    )
                                } ?: ""
                            }") {
                            +"Try Again"
                        }
                    }
                }
            }
        }
    }
    httpClient.close()
}

private fun isValidSubdomain(subdomain: String): Boolean {
    val pattern = "^[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\$".toRegex()
    return pattern.matches(subdomain) && subdomain.length in 1..63
}

// Helper function to check if domain is accessible
private suspend fun validateDomainAccessibility(domain: String): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            withTimeout(5.seconds) {
                val url = "https://$domain"
                val client = HttpClient()
                try {
                    val response: HttpResponse = client.head(url)
                    response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Found
                } catch (e: Exception) {
                    false
                } finally {
                    client.close()
                }
            }
        }
    } catch (e: Exception) {
        false
    }
}

class DomainNotAccessibleException(message: String) : Exception(message)