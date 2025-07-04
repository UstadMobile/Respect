package world.respect.datasource.ext

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import world.respect.datasource.DataLoadMetaInfo
import world.respect.datasource.DataLoadResult

suspend inline fun <reified T: Any> HttpClient.getDataLoadResult(
    url: Url
): DataLoadResult<T> {
    val response = this.get(url)
    val data = response.body<T>()
    return DataLoadResult(
        data = data,
        metaInfo = DataLoadMetaInfo.fromHttpMessage(url, response),
    )
}
