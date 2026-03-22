package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.appendIfNotNull
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.BookmarkDataSource
import world.respect.datalayer.school.BookmarkDataSource.Companion.PERSON_UID
import world.respect.datalayer.school.model.Bookmark
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory


class BookmarkDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : BookmarkDataSource, SchoolUrlBasedDataSource {

    private suspend fun BookmarkDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(BookmarkDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
                parameters.appendIfNotNull(PERSON_UID, personUid)
            }
            .build()
    }

    override fun getBookmarkStatus(
        personUid: String,
        url: Url
    ): Flow<Boolean> {
        throw IllegalArgumentException(
            "Bookmark status is not supported in HTTP datasource."
        )
    }


    override suspend fun store(list: List<Bookmark>) {
        httpClient.post(
            respectEndpointUrl(BookmarkDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: BookmarkDataSource.GetListParams
    ): DataLoadState<List<Bookmark>> {
        return httpClient.getAsDataLoadState<List<Bookmark>>(
            url = listParams.urlWithParams(),
            validationHelper = validationHelper
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun findBookmarksWithMissingPublication(personUid: String): List<Bookmark> {
        throw IllegalArgumentException(
            "Find Bookmarks is not supported in HTTP datasource."
        )
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: BookmarkDataSource.GetListParams
    ): Flow<DataLoadState<List<Bookmark>>> {
        return httpClient.getDataLoadResultAsFlow(
            urlFn = { listParams.urlWithParams() },
            dataLoadParams = loadParams,
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: BookmarkDataSource.GetListParams
    ): IPagingSourceFactory<Int, Bookmark> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { listParams.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Person>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                },
                logPrefixExtra =  { "Person-HTTP-listAsPagingSource(params=$listParams)" },
            )
        }
    }
}