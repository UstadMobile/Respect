package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.school.BookmarkDataSource
import world.respect.datalayer.school.BookmarkDataSourceLocal
import world.respect.datalayer.school.model.Bookmark
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.time.Clock

class BookmarkDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json,
) : BookmarkDataSourceLocal {

    override fun getBookmarkStatus(personUid: String, url: Url): Flow<Boolean> {
        return schoolDb.getBookmarkDao().getBookmarkStatus(
            personUid = personUid,
            url = url.toString()
        )
    }

    override suspend fun store(list: List<Bookmark>) {

        if (list.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = Clock.System.now()
                schoolDb.getBookmarkDao().upsert(
                    list.map {
                        it.copy(
                            stored = now
                        ).toEntities(uidNumberMapper).bookmark
                    }
                )
            }
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: BookmarkDataSource.GetListParams
    ): DataLoadState<List<Bookmark>> {
        return DataReadyState(
            data = schoolDb.getBookmarkDao()
                .list(
                    personUid = requireNotNull(listParams.personUid),
                    includeDeleted = listParams.common.includeDeleted ?: false
                )
                .map {
                    it.toModel(json)
                }
        )
    }

    override suspend fun findBookmarksWithMissingPublication(personUid: String): List<Bookmark> {
        return schoolDb.getBookmarkDao()
            .findBookmarksWithMissingPublication(personUid)
            .map { entity ->
                entity.toModel(json)
            }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: BookmarkDataSource.GetListParams
    ): Flow<DataLoadState<List<Bookmark>>> {
        return schoolDb.getBookmarkDao().listAsFlow(
            personUid = requireNotNull(listParams.personUid),
            includeDeleted = listParams.common.includeDeleted ?: false
        ).map { entityList ->
            DataReadyState(
                data = entityList.map { it.toModel(json) }
            )
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: BookmarkDataSource.GetListParams
    ): IPagingSourceFactory<Int, Bookmark> {
        return IPagingSourceFactory{
            schoolDb.getBookmarkDao().listAsPagingSource(
                personUid = requireNotNull(listParams.personUid),
                includeDeleted = listParams.common.includeDeleted ?: false
            ).map {
                it.toModel(json)
            }
        }
    }

    override suspend fun updateLocal(
    list: List<Bookmark>,
    forceOverwrite: Boolean
) {
    schoolDb.useWriterConnection { con ->
        con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
            val now = Clock.System.now()
            schoolDb.getBookmarkDao().upsert(
                bookmarks = list.filter { bookmark ->

                    forceOverwrite || schoolDb.getBookmarkDao().getBookmarkLastModified(
                        personUid = bookmark.personUid,
                        urlHash = uidNumberMapper(
                            bookmark.learningUnitManifestUrl.toString()
                        )
                    ).let { it ?: 0L } < bookmark.lastModified.toEpochMilliseconds()
                }.map {
                    it.copy(stored = now).toEntities(uidNumberMapper).bookmark
                }
            )
        }
    }
}

override suspend fun findByUidList(uids: List<String>): List<Bookmark> {
    return schoolDb.getBookmarkDao()
        .findByUidList(
            uids.map { uidNumberMapper(it) }
        )
        .map { it.toModel(json) }
}
}