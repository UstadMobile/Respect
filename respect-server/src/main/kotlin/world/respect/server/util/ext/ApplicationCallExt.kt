package world.respect.server.util.ext

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult.Page.Companion.COUNT_UNDEFINED
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.toHttpDate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.util.date.GMTDate
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import org.koin.core.scope.Scope
import org.koin.ktor.ext.getKoin
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLayerHeaders
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.lastModifiedForHttpResponseHeader
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.domain.GetPermissionLastModifiedUseCase
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.datalayer.shared.maxLastStoredOrNull
import world.respect.libutil.util.throwable.ForbiddenException
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The virtual host being used. Used on the server to scope dependencies.
 */
val ApplicationCall.virtualHost: Url
    get() = request.virtualHost

/**
 * Respect schools are handled using virtual hosting e.g. by subdomains. - see
 * SchoolDirectoryEntryScopeId and AppKoinModule.
 */
fun ApplicationCall.getSchoolKoinScope(): Scope {
    return getKoin().getOrCreateScope<SchoolDirectoryEntry>(
        SchoolDirectoryEntryScopeId(
            request.virtualHost, null
        ).scopeId
    )
}

fun ApplicationCall.requireAccountScope(): Scope {
    val authPrincipalId: UserIdPrincipal = principal() ?:
        throw ForbiddenException("Not authenticated")

    val scopeId = RespectAccountScopeId(
        request.virtualHost,
        AuthenticatedUserPrincipalId(authPrincipalId.name)
    ).scopeId

    return getKoin().getScopeOrNull(scopeId) ?: getKoin().createScope<RespectAccount>(scopeId).also {
        it.linkTo(getSchoolKoinScope())
    }
}


suspend inline fun <reified T: Any> ApplicationCall.respondOffsetLimitPaging(
    params: PagingSource.LoadParams<Int>,
    pagingSource: PagingSource<Int, T>,
    getPermissionLastModifiedUseCase: GetPermissionLastModifiedUseCase? = null,
) {
    val consistentThrough = Clock.System.now()

    getPermissionLastModifiedUseCase?.also { getPermissionLastMod ->
        response.header(
            name = DataLayerHeaders.XPermissionsLastModified,
            value = getPermissionLastMod().toString(),
        )
    }

    val pagingLoadResult = pagingSource.load(params)

    when(pagingLoadResult) {
        is PagingSource.LoadResult.Page -> {
            val unwrappedList = pagingLoadResult.data
            val firstItem = unwrappedList.firstOrNull()
            val modelsWithTimes = if(firstItem is ModelWithTimes) {
                @Suppress("UNCHECKED_CAST")
                unwrappedList as List<ModelWithTimes>
            }else {
                null
            }

            response.header(
                name = DataLayerHeaders.XConsistentThrough,
                value = consistentThrough.toString()
            )

            if(pagingLoadResult.itemsBefore != COUNT_UNDEFINED &&
                pagingLoadResult.itemsAfter != COUNT_UNDEFINED) {
                val totalItems = pagingLoadResult.itemsBefore + pagingLoadResult.itemsAfter +
                        pagingLoadResult.data.size
                response.header(DataLayerHeaders.XTotalCount, totalItems)
            }

            //As per README - the last-mod for validation purposes is actually the time stored,
            // not the time originally modified (possibly on other device).
            val maxLastStored = modelsWithTimes?.maxLastStoredOrNull()
            maxLastStored?.also { response.lastModified(it) }

            if(maxLastStored != null &&
                request.validateIfNotModifiedSince(maxLastStored)
            ) {
                respond(HttpStatusCode.NotModified)
                return
            }

            respond(message = unwrappedList)
        }

        is PagingSource.LoadResult.Error -> {
            throw pagingLoadResult.throwable
        }

        is PagingSource.LoadResult.Invalid<*, *> -> {
            respond(HttpStatusCode.BadRequest)
        }
    }
}

/**
 * Handles a response given a DataLoadState. This function will:
 *
 * a) Check incoming cache-validation headers: if-none-match and if-not-modified-since. If the
 *    validation passes, then will respond 302 not modified.
 * b) Add dataloadstate metadata headers to the response: Last-Modified, X-Consistent-Through,
 *    and etag.
 * c) If dataloadstate is NoDataLoaded with the reason not found, then will respond 404
 * d) If dataloadstate is DataReadyState then will respond with the data.
 *
 * @param dataLoadState the dataloadstate
 * @param typeInfo TypeInfo for the response as per ApplicationCall.respond
 */
suspend fun <T: Any> ApplicationCall.respondDataLoadState(
    dataLoadState: DataLoadState<T>,
    typeInfo: TypeInfo,
) {
    respondDataLoadState(
        dataLoadState = dataLoadState,
        onRespondWithData = { data ->
            this.respond(data, typeInfo)
        }
    )
}

/**
 * Handles a response given a DataLoadState, uses the default TypeInfo as per type parameter T.
 * This function will:
 *
 * a) Check incoming cache-validation headers: if-none-match and if-not-modified-since. If the
 *    validation passes, then will respond 302 not modified.
 * b) Add dataloadstate metadata headers to the response: Last-Modified, X-Consistent-Through,
 *    and etag.
 * c) If dataloadstate is NoDataLoaded with the reason not found, then will respond 404
 * d) If dataloadstate is DataReadyState then will respond with the data.
 *
 * @param dataLoadState the dataloadstate
 * @param T TypeInfo for the response
 */
suspend inline fun <reified T: Any> ApplicationCall.respondDataLoadState(
    dataLoadState: DataLoadState<T>,
) {
    respondDataLoadState(
        dataLoadState = dataLoadState,
        typeInfo = typeInfo<T>()
    )
}

/**
 * Handles a response given a DataLoadState. This function will:
 * a) Check incoming cache-validation headers: if-none-match and if-not-modified-since. If the
 *    validation passes, then will respond 302 not modified.
 * b) Add dataloadstate metadata headers to the response: Last-Modified, X-Consistent-Through,
 *    and etag.
 * c) If dataloadstate is NoDataLoaded with the reason not found, then will respond 404
 * d) If dataloadstate is DataReadyState then will respond with the data.
 *
 * @param dataLoadState the dataloadstate
 * @param onRespondWithData function to respond with the data. By default use the typeInfo. A custom
 *        onRespond function might not use the typeInfo at all.
 */
suspend fun <T: Any> ApplicationCall.respondDataLoadState(
    dataLoadState: DataLoadState<T>,
    onRespondWithData: suspend ApplicationCall.(T) -> Unit,
) {
    dataLoadState.metaInfo.etag?.also {
        response.header(HttpHeaders.ETag, it)
    }

    val lastModTimeStamp = dataLoadState.lastModifiedForHttpResponseHeader()

    lastModTimeStamp?.also {
        response.header(HttpHeaders.LastModified, GMTDate(it).toHttpDate())
    }

    dataLoadState.metaInfo.consistentThrough?.also { consistentThrough ->
        response.header(
            name = DataLayerHeaders.XConsistentThrough,
            value = consistentThrough.toString()
        )
    }

    dataLoadState.metaInfo.permissionsLastModified?.also { permissionsLastMod ->
        response.header(
            name = DataLayerHeaders.XPermissionsLastModified,
            value = permissionsLastMod.toString()
        )
    }

    if(lastModTimeStamp != null && request.validateIfNotModifiedSince(
            Instant.fromEpochMilliseconds(lastModTimeStamp)
    )) {
        respond(HttpStatusCode.NotModified)
        return
    }

    val ifNoneMatchRequestHeader = request.headers[HttpHeaders.IfNoneMatch]
    if(ifNoneMatchRequestHeader != null &&
        ifNoneMatchRequestHeader == dataLoadState.metaInfo.etag
    ) {
        respond(HttpStatusCode.NotModified)
        return
    }


    when {
        dataLoadState is DataReadyState -> {
            onRespondWithData(dataLoadState.data)
        }

        dataLoadState is NoDataLoadedState && dataLoadState.reason == NoDataLoadedState.Reason.NOT_FOUND -> {
            respond(HttpStatusCode.NotFound)
        }

        else -> {
            respond(HttpStatusCode.ServiceUnavailable)
        }
    }

}
