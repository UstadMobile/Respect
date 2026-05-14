package world.respect.lib.xapi.nanohttpd

import fi.iki.elonen.NanoHTTPD
import io.ktor.http.Url
import io.ktor.http.protocolWithAuthority
import io.ktor.util.StringValuesImpl
import io.ktor.util.decodeBase64String
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.ext.put
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.model.XapiStatementTransformingSerializer
import world.respect.lib.xapi.nanohttpd.ext.bodyAsBytes
import world.respect.lib.xapi.resources.XapiStatementsResource
import java.io.ByteArrayInputStream
import kotlin.uuid.Uuid

class XapiNanoHttpdApp(
    port: Int,
    private val json: Json,
    private val xapiResourceProvider: XapiNanoHttpdResourceProvider,
) : NanoHTTPD(port){

    /**
     * When serving /e/(endpointUrl)/ - the endpoint MUST be double encoded. NanoHTTPD will
     * 'helpfully' decode it, then we won't know what slashes are part of the endpoint and which
     * are part of the api path
     *
     * @param xapiUrl the url of the real upstream xapi server: this will be passed to
     *        XapiNanoHttpdResourceProvider to get the XapiResource
     */
    fun localUrlForEndpoint(
        xapiUrl: Url,
    ): Url {
        //LearningSpace must be double encoded - see note on serveendpoint
        val endpointEncoded = UrlEncoderUtil.encode(
            UrlEncoderUtil.encode(xapiUrl.toString())
        )
        return Url("http://127.0.0.1:$listeningPort${PATH_ENDPOINT_API}$endpointEncoded/")
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        val pathSegments = uri.substring(1).split("/")

        return when {
            uri.startsWith(PATH_ENDPOINT_API) -> {
                serveXapiEndpoint(session, pathSegments)
            }

            else -> {
                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found: $uri")
            }
        }
    }

    fun <T: Any> DataLoadState<T>.toFixedLengthResponse(
        serializer: SerializationStrategy<T>
    ): Response {
        val jsonText = this.dataOrNull()?.let {
            json.encodeToString(serializer, it)
        } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found")

        return newFixedLengthResponse(
            Response.Status.OK, "application/json", jsonText
        )
    }

    private fun serveXapiEndpoint(
        session: IHTTPSession,
        pathSegments: List<String>,
    ): Response {
        val endpointUrl = Url(UrlEncoderUtil.decode(pathSegments[1]))
        val authentication = session.headers["authorization"]

        fun basicAuth(): Pair<String, String> {
            if(authentication == null)
                throw IllegalStateException("no authentication provided")

            return authentication.substringAfter("Basic").trim().decodeBase64String()
                .split(":", limit = 2).let {
                    Pair(it.first(), it.last())
                }
        }

        return runBlocking {
            when(session.method) {
                /**
                 * Allow cross-origin requests as per
                 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Methods/OPTIONS
                 */
                Method.OPTIONS -> {
                    val referrer = session.headers["referer"] ?: throw IllegalArgumentException("No referrer")
                    val origin = Url(referrer).protocolWithAuthority


                    newFixedLengthResponse(
                        Response.Status.NO_CONTENT,
                        "application/json",
                        ByteArrayInputStream(byteArrayOf()),
                        0
                    ).also {
                        //https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Access-Control-Allow-Origin
                        it.addHeader("Access-Control-Allow-Origin", origin)

                        session.headers["access-control-request-method"]?.also { requestMethods ->
                            it.addHeader("Access-Control-Allow-Methods", requestMethods)
                        }

                        session.headers["access-control-request-headers"]?.also { requestHeaders ->
                            it.addHeader("Access-Control-Allow-Headers", requestHeaders)
                        }
                    }
                }

                Method.GET -> {
                    val (authUser, _) = basicAuth()
                    val dataLoadState = xapiResourceProvider(endpointUrl, authUser).get(
                        listParams = XapiStatementsResource.GetStatementParams.fromParams(
                            params = StringValuesImpl(
                                caseInsensitiveName = false,
                                values = session.parameters
                            ),
                            json = json,
                        )
                    )

                    dataLoadState.toFixedLengthResponse(XapiStatementResult.serializer())
                }

                Method.POST -> {
                    val (authUser, _) = basicAuth()
                    val postBody = session.bodyAsBytes()?.let {
                        json.decodeFromString(
                            deserializer = ListSerializer(XapiStatementTransformingSerializer),
                            string = it.decodeToString()
                        )
                    } ?: throw IllegalArgumentException()

                    val uuidsCreated = xapiResourceProvider(endpointUrl, authUser).post(
                        list = postBody
                    )

                    newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        json.encodeToString(ListSerializer(Uuid.serializer()), uuidsCreated)
                    )
                }

                Method.PUT -> {
                    val (authUser, _) = basicAuth()
                    xapiResourceProvider(endpointUrl, authUser).put(
                        statementId = session.parameters["statementId"]?.first()?.let {
                                Uuid.parse(it)
                            } ?: throw IllegalArgumentException("Statements PUT requires statementId"),
                        statement = session.bodyAsBytes()?.decodeToString()?.let {
                            json.decodeFromString(XapiStatement.serializer(), it)
                        } ?: throw IllegalArgumentException("No body")
                    )

                    newFixedLengthResponse(
                        Response.Status.NO_CONTENT,
                        "application/json",
                        ByteArrayInputStream(byteArrayOf()),
                        0,
                    )
                }

                else -> {
                    newFixedLengthResponse(
                        Response.Status.METHOD_NOT_ALLOWED,
                        "text/plain",
                        ByteArrayInputStream(byteArrayOf()),
                        0,
                    )
                }
            }
        }


    }


    companion object {

        const val PATH_ENDPOINT_API = "/e/"

    }
}