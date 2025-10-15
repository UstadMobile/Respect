package world.respect.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.sourceforge.argparse4j.inf.Namespace
import world.respect.datalayer.opds.model.LangMapStringValue
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.server.domain.school.add.AddSchoolUseCase
import world.respect.server.domain.school.add.AddSchoolUseCase.Companion.DEFAULT_ADMIN_USERNAME
import java.io.File
import java.util.Properties
import kotlin.system.exitProcess
import kotlin.time.Clock

fun managerServerMain(ns: Namespace) {
    val json = Json { encodeDefaults = true }
    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json = json)
        }
    }

    val dataDir = ns.getString("datadir")?.let { File(it) }
        ?: File("${ktorAppHomeDir().absolutePath}/$DEFAULT_DATA_DIR_NAME")
    println("DataDir=$dataDir")

    val serverPropertiesFile = ktorServerPropertiesFile(dataDir = dataDir)

    if(!serverPropertiesFile.exists()) {
        println("Error: Server is not running: server.properties does not exist")
        exitProcess(1)
    }

    val serverProperties = Properties()
    serverPropertiesFile.reader().use { serverPropertiesReader ->
        serverProperties.load(serverPropertiesReader)
    }

    val port = serverProperties.getProperty(SERVER_PROPERTIES_KEY_PORT)

    val systemConfigAuth = File(dataDir, DIRECTORY_ADMIN_FILENAME).readText().trim()

    println("Connect to $port using auth $systemConfigAuth")

    val serverUrl = Url("http://localhost:$port/")
    val authHeader = "Basic ${"admin:$systemConfigAuth".encodeBase64()}"

    runBlocking {
        when(ns.getString("subparser_name")) {
            CMD_ADD_SCHOOL -> {
                val schoolBaseUrl = Url(ns.getString("url"))
                val rpId = ns.getString("rpId") ?: schoolBaseUrl.host

                val response = httpClient.post(
                    serverUrl.appendEndpointSegments("api/directory/school")
                ) {
                    header(HttpHeaders.Authorization, authHeader)
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            AddSchoolUseCase.AddSchoolRequest(
                                school = SchoolDirectoryEntry(
                                    name = LangMapStringValue(ns.getString("name")),
                                    self = schoolBaseUrl,
                                    xapi = schoolBaseUrl.appendEndpointSegments("api/school/xapi"),
                                    oneRoster = schoolBaseUrl.appendEndpointSegments("api/school/oneroster"),
                                    respectExt = schoolBaseUrl.appendEndpointSegments("api/school/respect"),
                                    //Will be set on server
                                    rpId = rpId,
                                    lastModified = Clock.System.now(),
                                    stored = Clock.System.now(),
                                ),
                                dbUrl = ns.getString("dburl") ?: schoolBaseUrl.sanitizedForFilename(),
                                adminUsername = ns.getString("adminusername") ?: DEFAULT_ADMIN_USERNAME,
                                adminPassword = ns.getString("adminpassword"),
                            )
                        )
                    )
                }
                println("Response: ${response.status}")
            }
        }

        exitProcess(0)
    }
}