package world.respect.datalayer.http.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.util.reflect.TypeInfo
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLayerHeaders
import world.respect.datalayer.DataLayerParams
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.NoDataLoadedState.Reason
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.shared.paging.CacheableHttpPagingSource
import world.respect.datalayer.shared.paging.getClippedRefreshKey
import world.respect.datalayer.shared.paging.getLimit
import world.respect.datalayer.shared.paging.getOffset

/**
 * Fundamentally an http based paging source could be one of the two:
 *  a) Offset/limit based where it is possible, given known offset and limit values, to construct a
 *     corresponding http request (e.g. the parameter names for offset and limit are known).
 *  b) URL based where there is no defined schema or way to know what the URL / request parameters
 *     should be for a given limit or offset, and it is required to follow a chain of next/prev links
 *     retrieved from the responses (e.g. Link header, Opds data itself, etc)
 *
 *  This implements the Offset/limit scenario. It can therefor neatly align Room's PagingSource.
 */
class OffsetLimitHttpPagingSource<T: Any>(
    private val baseUrlProvider: suspend () -> Url,
    private val httpClient: HttpClient,
    private val validationHelper: BaseDataSourceValidationHelper? = null,
    private val typeInfo: TypeInfo,
    private val requestBuilder: HttpRequestBuilder.() -> Unit = { },
    tag: String? = null,
) : PagingSource<Int, T>(), CacheableHttpPagingSource<Int, T> {

    private val logPrefix = "RPaging/OffsetLimitHttpPagingSource(tag = $tag)"

    private var lastKnownTotalCount = -1

    private val metadataMap = mutableMapOf<LoadResult<Int, T>, DataLoadMetaInfo>()

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.getClippedRefreshKey()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            Napier.d("$logPrefix load key=${params.key}")
            val key = params.key ?: 0
            val limit: Int = getLimit(params, key)

            val offset = when {
                /*
                 * The item count as per the getOffset function in Room is needed when handling
                 * LoadParams.Prepend. Most of the time, we would know this from a previous request. In
                 * the rare instances we don't, make a http head request to get it. If the server does
                 * not provide the X-Total-Count, then we need to return LoadResult.Invalid
                 */
                params is LoadParams.Prepend<*> && lastKnownTotalCount < 0 -> {
                    TODO("Make an http request to get total item count or return invalid")
                }

                /*
                 * Where we already know the total item count or we are looking to load the first page,
                 * we can use the getOffset function
                 */
                (lastKnownTotalCount >= 0 || key == 0) -> {
                    getOffset(params, key, lastKnownTotalCount)
                }

                else -> {
                    key
                }
            }

            val url = URLBuilder(baseUrlProvider()).apply {
                parameters.append(DataLayerParams.OFFSET, offset.toString())
                parameters.append(DataLayerParams.LIMIT, limit.toString())
            }.build()

            Napier.d("$logPrefix: offsetlimit loading from $url")

            val listLoadState: DataLoadState<List<T>> = httpClient.getAsDataLoadState(
                url, typeInfo, validationHelper,
            ) {
                requestBuilder()
            }

            if(listLoadState !is DataReadyState) {
                return when {
                    listLoadState is NoDataLoadedState && listLoadState.reason == Reason.NOT_MODIFIED -> {
                        LoadResult.Error(CacheableHttpPagingSource.NotModifiedNonException())
                    }

                    listLoadState is NoDataLoadedState && listLoadState.reason == Reason.NOT_FOUND -> {
                        LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null,
                            itemsAfter = 0,
                            itemsBefore = 0,
                        )
                    }

                    listLoadState is DataErrorResult -> {
                        LoadResult.Error(listLoadState.error)
                    }

                    else -> {
                        LoadResult.Error(
                            IllegalStateException("OffsetLimitPagingSource: Invalid state: $listLoadState")
                        )
                    }
                }
            }

            val itemCount = listLoadState.metaInfo.headers?.get(DataLayerHeaders.XTotalCount)?.toInt()?.also {
                lastKnownTotalCount = it
            } ?: -1

            val data: List<T> = listLoadState.data

            Napier.d("$logPrefix offsetlimit loaded ${data.size} items")

            //This section is largely based on RoomUtil.queryDatabase function
            val nextPosToLoad = offset + data.size
            val nextKey =
                if (data.isEmpty() || data.size < limit || nextPosToLoad >= itemCount) {
                    null
                } else {
                    nextPosToLoad
                }
            val prevKey = if (offset <= 0 || data.isEmpty()) null else offset


            return LoadResult.Page(
                data = data,
                prevKey = prevKey,
                nextKey = nextKey,
                itemsBefore = offset,
                itemsAfter = maxOf(0, itemCount - nextPosToLoad)
            ).also {
                metadataMap[it] = listLoadState.metaInfo
            }
        }catch(e: Throwable) {
            Napier.e("$logPrefix exception loading", throwable = e)
            LoadResult.Error(e)
        }
    }

    override suspend fun onLoadResultStored(loadResult: LoadResult<Int, T>) {
        if(loadResult is LoadResult.Page) {
            val metaData = metadataMap[loadResult] ?: return
            (validationHelper as? ExtendedDataSourceValidationHelper)?.updateValidationInfo(
                metaData
            )
        }
    }

}
