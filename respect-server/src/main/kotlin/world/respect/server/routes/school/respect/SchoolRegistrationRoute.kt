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
import kotlinx.html.p
import kotlinx.html.title
import org.koin.ktor.ext.inject
import world.respect.server.SchoolConfig
import world.respect.server.domain.school.add.RegisterSchoolUseCase
import world.respect.server.util.ext.getStatusCode
import java.net.URLEncoder

fun Route.SchoolRegistrationRoute() {
    val registerSchoolUseCase: RegisterSchoolUseCase by inject()
    val schoolConfig: SchoolConfig by inject()

    // Show registration form
    get("/register-school") {
        // Check if registration is enabled
        if (!schoolConfig.registration.enabled) {
            call.respondText(
                "School registration is disabled",
                status = io.ktor.http.HttpStatusCode.Forbidden
            )
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

    // Handle form submission - now minimal logic
    post("/register-school") {
        val parameters = call.receiveParameters()

        // Get input parameters
        val schoolName = parameters["schoolName"] ?: ""
        val redirectUrl = parameters["redirect"] ?: "world.respect.app://school-registered"

        try {
            // Determine the school URL based on registration mode
            val schoolUrl = when (schoolConfig.registration.mode) {
                SchoolConfig.RegistrationConfig.RegistrationMode.SUBDOMAIN -> {
                    val schoolSubdomain = parameters["schoolName"] ?: ""
                    if (schoolSubdomain.isBlank()) {
                        call.respondText(
                            "School subdomain is required",
                            status = io.ktor.http.HttpStatusCode.BadRequest
                        )
                        return@post
                    }
                    // Construct the URL on the server side
                    "https://$schoolSubdomain.${schoolConfig.registration.topLevelDomain}"
                }

                SchoolConfig.RegistrationConfig.RegistrationMode.ANY_URL -> {
                    parameters["schoolUrl"] ?: ""
                }

                else -> {
                    call.respondText(
                        "Registration mode not supported",
                        status = io.ktor.http.HttpStatusCode.BadRequest
                    )
                    return@post
                }
            }

            // Validate that we have a school URL
            if (schoolUrl.isBlank()) {
                call.respondText(
                    "School URL is required",
                    status = io.ktor.http.HttpStatusCode.BadRequest
                )
                return@post
            }

            // Invoke the use case with all the logic
            val response = registerSchoolUseCase(
                RegisterSchoolUseCase.RegisterSchoolRequest(
                    schoolName = schoolName,
                    schoolUrl = schoolUrl,
                    redirectUrl = redirectUrl
                )
            )

            // Build redirect URL with schoolUrl parameter
            val encodedSchoolUrl = URLEncoder.encode(response.schoolUrl, "UTF-8")
            val redirectWithParams = if (response.redirectUrl.contains("?")) {
                "${response.redirectUrl}&schoolUrl=$encodedSchoolUrl"
            } else {
                "${response.redirectUrl}?schoolUrl=$encodedSchoolUrl"
            }

            call.respondRedirect(redirectWithParams)

        } catch (e: Exception) {

            val statusCode = e.getStatusCode() ?: io.ktor.http.HttpStatusCode.InternalServerError

            call.respondHtml(statusCode) {
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