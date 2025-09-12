package world.respect.datalayer.http.school

import androidx.paging.PagingSource
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.Url
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
import world.respect.datalayer.http.ext.appendListParams
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.adapters.asListDetails
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.shared.paging.map
import world.respect.datalayer.shared.params.GetListCommonParams
import kotlin.time.Instant

class PersonDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryDataSource: SchoolDirectoryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper,
) : PersonDataSource, SchoolUrlBasedDataSource {

    private suspend fun PersonDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(PersonDataSource.ENDPOINT_NAME))
            .apply { parameters.appendListParams(common) }
            .build()
    }

    override suspend fun getAllUsers(sourcedId: String): List<Person> {
        TODO("Not yet implemented")
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
            headers[HttpHeaders.Authorization] = "Bearer ${tokenProvider.provideToken().accessToken}"
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
            headers[HttpHeaders.Authorization] = "Bearer ${tokenProvider.provideToken().accessToken}"
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
    ): PagingSource<Int, Person> {
        return OffsetLimitHttpPagingSource(
            baseUrlProvider = { params.urlWithParams() },
            httpClient = httpClient,
            validationHelper = validationHelper,
            typeInfo = typeInfo<List<Person>>(),
            requestBuilder = {
                headers[HttpHeaders.Authorization] = "Bearer ${tokenProvider.provideToken().accessToken}"
                headers[HttpHeaders.CacheControl] = "no-store" //prevent 'normal' cache
            },
            tag = "Person-HTTP",
        )
    }

    override fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: PersonDataSource.GetListParams
    ): PagingSource<Int, PersonListDetails> {
        return OffsetLimitHttpPagingSource<Person>(
            baseUrlProvider = { listParams.urlWithParams() },
            httpClient = httpClient,
            validationHelper = validationHelper,
            typeInfo = typeInfo<List<Person>>(),
        ).map { person ->
            person.asListDetails()
        }
    }

    override suspend fun store(persons: List<Person>) {
        throw IllegalStateException("Person-store-http: Not yet supported")
    }
}