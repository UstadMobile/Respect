package world.respect.server

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import world.respect.Greeting
import world.respect.libutil.ext.randomString
import world.respect.server.routes.AUTH_CONFIG_DIRECTORY_ADMIN_BASIC
import world.respect.server.routes.AuthRoute
import world.respect.server.routes.RespectSchoolDirectoryRoute
import world.respect.server.routes.getRespectSchoolJson
import java.io.File
import java.util.Properties
import io.ktor.server.plugins.swagger.*
import okio.withLock
import org.koin.core.qualifier.TypeQualifier
import org.koin.ktor.ext.inject
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.libutil.util.throwable.ExceptionWithHttpStatusCode
import world.respect.server.routes.passkey.GetAllActivePasskeysRoute
import world.respect.server.routes.passkey.RevokePasskeyRoute
import world.respect.server.routes.passkey.VerifySignInWithPasskeyRoute
import world.respect.server.routes.school.respect.AddChildAccountRoute
import world.respect.server.routes.school.respect.AssignmentRoute
import world.respect.server.routes.school.respect.ClassRoute
import world.respect.server.routes.school.respect.EnrollmentRoute
import world.respect.server.routes.school.respect.InviteInfoRoute
import world.respect.server.routes.school.respect.PersonPasskeyRoute
import world.respect.server.routes.school.respect.PersonPasswordRoute
import world.respect.server.routes.school.respect.PersonRoute
import world.respect.server.routes.school.respect.RedeemInviteRoute
import world.respect.server.routes.school.respect.SchoolAppRoute
import world.respect.server.routes.username.UsernameSuggestionRoute
import world.respect.server.util.ext.getSchoolKoinScope
import world.respect.server.util.ext.virtualHost
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.validateauth.ValidateAuthorizationUseCase
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import java.util.concurrent.locks.ReentrantLock

const val AUTH_CONFIG_SCHOOL = "auth-school-bearer"

@Suppress("unused") // Used via application.conf
fun Application.module() {

    val serverProperties = Properties().apply {
        setProperty(SERVER_PROPERTIES_KEY_PORT, environment.config.port.toString())
    }

    environment.config.absoluteDataDir().takeIf { !it.exists() }?.mkdirs()

    ktorServerPropertiesFile(
        dataDir = environment.config.absoluteDataDir()
    ).writer().use { serverPropWriter ->
        serverProperties.store(serverPropWriter, null)
    }

    val wellKnownDir = File(ktorAppHomeDir(), "well-known")
    val assetLinksFile = File(wellKnownDir, "assetlinks.json")

    val dirAdminFile = File(environment.config.absoluteDataDir(), DIRECTORY_ADMIN_FILENAME)
    dirAdminFile.takeIf { !it.exists() }?.also {
        it.writeText(randomString(DEFAULT_DIR_ADMIN_PASS_LENGTH))
    }

    install(Koin) {
        slf4jLogger()
        modules(serverKoinModule(environment.config))
    }

    val json = getKoin().get<Json>()
    install(ContentNegotiation) {
        json(
            json = json,
            contentType = ContentType.Application.Json
        )
    }

    val scopeLock = ReentrantLock()


    install(Authentication) {
        basic(AUTH_CONFIG_DIRECTORY_ADMIN_BASIC) {
            realm = "Access realm directory admin"
            validate { credentials ->
                val adminPassword = dirAdminFile.readText().trim()
                if(credentials.password == adminPassword) {
                    UserIdPrincipal(credentials.name)
                }else {
                    null
                }
            }
        }

        /*
         * School authentication
         */
        bearer(AUTH_CONFIG_SCHOOL) {
            realm = "Access school"
            authenticate { tokenCredential ->
                val schoolScopeId = SchoolDirectoryEntryScopeId(request.virtualHost, null)
                val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
                    schoolScopeId.scopeId
                )
                val validateAuthorizationUseCase: ValidateAuthorizationUseCase = schoolScope.get()

                validateAuthorizationUseCase(
                    ValidateAuthorizationUseCase.BearerTokenCredential(tokenCredential.token)
                )?.let {
                    /*
                     * Create the account scope (if needed) and link to the related School Scope.
                     * Because all account level DI is done using factory (which can be called
                     * concurrently), we need to use a lock when creating and linking the scope.
                     */
                    scopeLock.withLock {
                        val authenticatedPrincipal = AuthenticatedUserPrincipalId(it.guid)
                        val accountScopeId = RespectAccountScopeId(schoolScopeId.schoolUrl, authenticatedPrincipal)
                        val accountScope = getKoin().getScopeOrNull(accountScopeId.scopeId)
                        if(accountScope == null) {
                            val accountScope = getKoin().createScope(
                                accountScopeId.scopeId, TypeQualifier(RespectAccount::class)
                            )
                            accountScope.linkTo(schoolScope)
                        }

                        UserIdPrincipal(it.guid)
                    }
                }
            }
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()

            if(cause is ExceptionWithHttpStatusCode) {
                val responseText = cause.message
                val httpStatus = HttpStatusCode.fromValue(cause.statusCode)
                if(responseText != null) {
                    call.respondText(text = responseText, status = httpStatus)
                }else {
                    call.respond(httpStatus)
                }
            }else {
                call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            }
        }
    }

    //As per https://ktor.io/docs/server-swagger-ui.html#configure-cors
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        route(".well-known") {
            getRespectSchoolJson("respect-school.json")

            get("assetlinks.json") {
                call.respondFile(assetLinksFile)
            }
        }

        swaggerUI(
            path = "swagger",
            swaggerFile = "openapi/openapi.yaml",
        )

        route("api") {
            route("passkey"){

                VerifySignInWithPasskeyRoute(
                    useCase =  { it.getSchoolKoinScope().get() }
                )

                GetAllActivePasskeysRoute(
                    useCase =  { it.getSchoolKoinScope().get() }
                )
                RevokePasskeyRoute(
                    useCase =  { it.getSchoolKoinScope().get() }
                )
            }
            route("directory") {
                val respectAppDataSource: RespectAppDataSource by inject()
                RespectSchoolDirectoryRoute(respectAppDataSource)
            }

            route("school") {
                route("respect") {
                    AddChildAccountRoute(
                        addChildAccountUseCase = { it.getSchoolKoinScope().get() }
                    )
                    route("auth") {
                        AuthRoute()
                    }
                    route("invite") {
                        RedeemInviteRoute(
                            redeemInviteUseCase = { it.getSchoolKoinScope().get() }
                        )
                        InviteInfoRoute(
                            getInviteInfoUseCase = { it.getSchoolKoinScope().get() }
                        )
                    }
                    route("username"){
                        UsernameSuggestionRoute(
                            usernameSuggestionUseCase = { it.getSchoolKoinScope().get() }
                        )
                    }
                    authenticate(AUTH_CONFIG_SCHOOL) {
                        SchoolAppRoute()
                        PersonRoute()
                        PersonPasskeyRoute()
                        PersonPasswordRoute()
                        ClassRoute()
                        EnrollmentRoute()
                        AssignmentRoute()
                    }
                }
            }
        }
    }
}
