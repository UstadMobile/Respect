package world.respect.lib.xapi.nanohttpd

import fi.iki.elonen.NanoHTTPD
import io.ktor.http.Url
import io.ktor.util.StringValuesImpl
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.model.XapiStatementTransformingSerializer
import world.respect.lib.xapi.nanohttpd.ext.bodyAsBytes
import world.respect.lib.xapi.resources.XapiStatementsResource
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
        return Url("http://127.0.0.1:$listeningPort${PATH_ENDPOINT_API}$endpointEncoded}/")
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
        val authentication = session.headers["authentication"] ?: ""

        val xapiResource = xapiResourceProvider(endpointUrl, authentication)

        return runBlocking {
            when(session.method) {
                Method.GET -> {
                    val dataLoadState = xapiResource.get(
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
                    val postBody = session.bodyAsBytes()?.let {
                        json.decodeFromString(
                            deserializer = ListSerializer(XapiStatementTransformingSerializer),
                            string = it.decodeToString()
                        )
                    } ?: throw IllegalArgumentException()

                    val uuidsCreated = xapiResource.post(
                        list = postBody
                    )

                    newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        json.encodeToString(ListSerializer(Uuid.serializer()), uuidsCreated)
                    )
                }

                Method.PUT -> {
                    TODO()
                }

                else -> {
                    TODO()
                }
            }
        }


    }


    companion object {

        const val PATH_ENDPOINT_API = "/e/"

    }
}