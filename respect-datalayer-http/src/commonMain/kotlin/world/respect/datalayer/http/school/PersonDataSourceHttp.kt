package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.DataLayerParams
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.appendIfNotNull
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.adapters.asListDetails
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import world.respect.datalayer.shared.params.GetListCommonParams
import kotlin.time.Instant

class PersonDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : PersonDataSource, SchoolUrlBasedDataSource {

    private suspend fun PersonDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(PersonDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_CLASS_UID, filterByClazzUid)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_ENROLLMENT_ROLE, filterByEnrolmentRole?.value)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_NAME, filterByName)
            }
            .build()
    }

    override suspend fun findByUsername(username: String): Person? {
        TODO("Not yet implemented")
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Person> {
        return httpClient.getAsDataLoadState<List<Person>>(
            PersonDataSource.GetListParams(
                GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> {
        return httpClient.getDataLoadResultAsFlow<List<Person>>(
            urlFn = {
                PersonDataSource.GetListParams(
                    GetListCommonParams(guid = guid)
                ).urlWithParams()
            },
            dataLoadParams = DataLoadParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.map {
            it.firstOrNotLoaded()
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<Person>>> {
        return httpClient.getDataLoadResultAsFlow<List<Person>>(
            urlFn = {
                PersonDataSource.GetListParams(
                    GetListCommonParams(searchQuery = searchQuery)
                ).urlWithParams()
            },
            dataLoadParams = loadParams,
            validationHelper = validationHelper,
        ) {
            headers[HttpHeaders.Authorization] = "Bearer ${tokenProvider.provideToken().accessToken}"
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        searchQuery: String?,
        since: Instant?,
    ): DataLoadState<List<Person>> {
        return httpClient.getAsDataLoadState<List<Person>>(
            url = URLBuilder(respectEndpointUrl(PersonDataSource.ENDPOINT_NAME)).apply {
                since?.also {
                    parameters.append(DataLayerParams.SINCE, it.toString())
                }
            }.build(),
            validationHelper = validationHelper,
        ) {
            val token = tokenProvider.provideToken()
            println("PersonDataSource: load person list using token $token")
            headers[HttpHeaders.Authorization] = "Bearer ${token.accessToken}"
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): IPagingSourceFactory<Int, Person> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Person>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                },
                tag = "Person-HTTP",
            )
        }
    }

    override fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: PersonDataSource.GetListParams
    ): IPagingSourceFactory<Int, PersonListDetails> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource<Person>(
                baseUrlProvider = { listParams.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Person>>(),
            ).map { person ->
                person.asListDetails()
            }
        }
    }

    override suspend fun store(list: List<Person>) {
        httpClient.post(
            url = respectEndpointUrl(PersonDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}