package world.respect.datalayer.ext

import com.ustadmobile.ihttp.headers.asIHttpHeaders
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.etag
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper

suspend fun <T: Any> HttpClient.getAsDataLoadState(
    url: Url,
    typeInfo: TypeInfo,
    validationHelper: BaseDataSourceValidationHelper? = null,
    block: HttpRequestBuilder.() -> Unit = { },
): DataLoadState<T> {
    return try {
        val response = this.get(url) {
            block()
            //note the block can change the URL (eg by adding parameters), so get validationInfo
            //after running block
            validationHelper?.also { addCacheValidationHeaders(it) }
        }

        return if(response.status == HttpStatusCode.NotModified) {
            NoDataLoadedState.notModified()
        }else {
            val data = response.body<T>(typeInfo)
            val extendedValidationHelper = validationHelper as? ExtendedDataSourceValidationHelper
            val varyHeader = response.headers.getAll(HttpHeaders.Vary)
                ?.joinToString(separator = ",")
            val validationInfoKey = extendedValidationHelper?.validationInfoKey(
                response.request.headers.asIHttpHeaders(),
                response.headers.getAll(HttpHeaders.Vary)?.joinToString(separator = ",")
            )

            DataReadyState(
                data = data,
                metaInfo = DataLoadMetaInfo(
                    url = response.request.url,
                    lastModified = response.lastModifiedAsLong(),
                    etag = response.etag(),
                    consistentThrough = response.consistentThroughAsLong(),
                    validationInfoKey = validationInfoKey ?: 0,
                    varyHeader = varyHeader,
                )
            )
        }
    }catch(t: Throwable) {
        Napier.d("Exception loading $url", t)
        DataErrorResult(
            error = t,
            metaInfo = DataLoadMetaInfo(url = url)
        )
    }
}

suspend inline fun <reified T: Any> HttpClient.getAsDataLoadState(
    url: Url,
    validationHelper: BaseDataSourceValidationHelper? = null,
    noinline block: HttpRequestBuilder.() -> Unit = { },
): DataLoadState<T> {
    return getAsDataLoadState(url, typeInfo<T>(), validationHelper, block)
}

inline fun <reified T: Any> HttpClient.getDataLoadResultAsFlow(
    url: Url,
    dataLoadParams: DataLoadParams,
    validationHelper: BaseDataSourceValidationHelper? = null,
    noinline block: HttpRequestBuilder.() -> Unit = { },
): Flow<DataLoadState<T>> {
    return getDataLoadResultAsFlow(
        urlFn = { url },
        dataLoadParams = dataLoadParams,
        validationHelper = validationHelper,
        block = block,
    )
}

/**
 * @param urlFn Data source functions often return a flow, however sometimes figuring out the url
 *        itself requires a suspended function (such as a database query). The function
 *        that returns the flow is not suspended. Accepting a function parameter makes it easier to
 *        shift the suspended operation to get the url into the flow.
 */
inline fun <reified T: Any> HttpClient.getDataLoadResultAsFlow(
    noinline urlFn: suspend () -> Url,
    @Suppress("unused") dataLoadParams: DataLoadParams,
    validationHelper: BaseDataSourceValidationHelper? = null,
    noinline block: HttpRequestBuilder.() -> Unit = { },
): Flow<DataLoadState<T>> {
    return flow {
        val urlVal = urlFn()
        emit(DataLoadingState(metaInfo = DataLoadMetaInfo(url = urlVal)))

        emit(getAsDataLoadState(urlVal, validationHelper, block = block))
    }
}

