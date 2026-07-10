package world.respect.datalayer.ext

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.etag
import io.ktor.util.reflect.TypeInfo
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState

suspend fun <T: Any> HttpResponse.toDataLoadState(
    typeInfo: TypeInfo,
    validationInfoKey: Long? = null,
): DataLoadState<T> {
    val varyHeader = headers.getAll(HttpHeaders.Vary)
        ?.joinToString(separator = ",")

    val metaInfo = DataLoadMetaInfo(
        url = request.url,
        lastModified = lastModifiedAsLong(),
        etag = etag(),
        consistentThrough = consistentThrough(),
        validationInfoKey = validationInfoKey ?: 0,
        varyHeader = varyHeader,
        permissionsLastModified = permissionsLastModified(),
        headers = headers,
    )

    return if(status == HttpStatusCode.NotModified) {
        NoDataLoadedState.notModified(metaInfo = metaInfo)
    }else {
        val data = body<T>(typeInfo)

        DataReadyState(
            data = data,
            metaInfo = metaInfo
        )
    }

}