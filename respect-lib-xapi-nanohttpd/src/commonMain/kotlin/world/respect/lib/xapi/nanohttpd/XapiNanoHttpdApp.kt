package world.respect.lib.xapi.nanohttpd

import fi.iki.elonen.NanoHTTPD
import io.ktor.http.Url
import io.ktor.util.StringValuesImpl
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
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
    private val xapiResourceProvider: XapiResourceProvider,
) : NanoHTTPD(port){

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        val pathSegments = uri.substring(1).split("/")

        return when {
            uri.startsWith(PATH_ENDPOINT_API) -> {
                serverXapiEndpoint(session, pathSegments)
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


    private fun serverXapiEndpoint(
        session: IHTTPSession,
        pathSegments: List<String>,
    ): Response {
        val endpointUrl = Url(UrlEncoderUtil.decode(pathSegments[1]))
        val xapiResource = xapiResourceProvider(endpointUrl)

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

                    dataLoadState.toFixedLengthResponse(
                        XapiStatementResult.serializer(),
                    )
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