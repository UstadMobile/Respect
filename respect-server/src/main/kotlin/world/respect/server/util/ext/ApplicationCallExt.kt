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
import org.koin.core.scope.Scope
import org.koin.ktor.ext.getKoin
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLayerHeaders
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.ext.lastModifiedForHttpResponseHeader
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
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
    pagingSource: PagingSource<Int, T>
) {
    val consistentThrough = Clock.System.now()
    val pagingLoadResult = pagingSource.load(params)

    when(pagingLoadResult) {
        is PagingSource.LoadResult.Page -> {
            val firstItem = pagingLoadResult.data.firstOrNull()
            val modelsWithTimes = if(firstItem is ModelWithTimes) {
                @Suppress("UNCHECKED_CAST")
                pagingLoadResult.data as List<ModelWithTimes>
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
            maxLastStored?.also {
                response.header(
                    HttpHeaders.LastModified,
                    GMTDate(it.toEpochMilliseconds()).toHttpDate()
                )
            }

            if(maxLastStored != null &&
                request.validateIfNotModifiedSince(maxLastStored)
            ) {
                respond(HttpStatusCode.NotModified)
                return
            }

            respond(pagingLoadResult.data)
        }

        else -> {
            //TODO: Respond with error code.
            //nothing yet
        }
    }
}


/**
 * Handles a response given a DataReadyState. Will automatically handle responding with 304 not
 * modified if the request has an If-Modified-Since header or If-None-Match header.
 *
 * It will also add the etag and last modified dates from the DataReadyState metainfo to the response.
 */
suspend inline fun <reified T: Any> ApplicationCall.respondDataLoadState(
    dataLoadState: DataLoadState<T>
) {
    dataLoadState.metaInfo.etag?.also {
        response.header(HttpHeaders.ETag, it)
    }

    val lastModTimeStamp = dataLoadState.lastModifiedForHttpResponseHeader()

    lastModTimeStamp?.also {
        response.header(HttpHeaders.LastModified, GMTDate(it).toHttpDate())
    }

    if(dataLoadState.metaInfo.consistentThrough > 0) {
        response.header(
            name = DataLayerHeaders.XConsistentThrough,
            value = Instant.fromEpochMilliseconds(dataLoadState.metaInfo.consistentThrough)
                .toString()
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

    if(dataLoadState is DataReadyState) {
        respond(dataLoadState.data)
    }else {
        respond(HttpStatusCode.ServiceUnavailable)
    }

}
