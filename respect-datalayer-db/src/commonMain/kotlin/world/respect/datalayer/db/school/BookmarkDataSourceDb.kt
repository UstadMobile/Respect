package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
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
import kotlin.time.Clock

class BookmarkDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,


) : BookmarkDataSourceLocal {

    override fun getBookmarkStatus(personUid: String, url: Url): Flow<Boolean> {
        return schoolDb.getBookmarkDao().getBookmarkStatus(
            personUid = personUid,
            manifestUrl = url.toString()
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
        listParams: BookmarkDataSource.GetListParams,

        ): DataLoadState<List<Bookmark>> {
        val entities = schoolDb.getBookmarkDao()
            .list(
                personUid = requireNotNull(listParams.personUid),
                includeDeleted = listParams.common.includeDeleted ?: false
            )

        entities.forEach {
            println(
                """
        BOOKMARK DEBUG
        bookmarkUrl=${it.bookmark.bLearningUnitManifestUrl}
        bookmarkHash=${it.bookmark.bLearningUnitUrlHash}
        publicationHash=${it.publication?.publication?.opeUrlHash}
        publicationUid=${it.publication?.publication?.opeUid}
        langMaps=${it.publication?.langMaps?.size}
        """.trimIndent()
            )
        }
        return DataReadyState(
            data = entities.map { it.toModel() }
        )
     /*   return DataReadyState(
            data = schoolDb.getBookmarkDao()
                .list(
                    personUid = requireNotNull(listParams.personUid),
                    includeDeleted = listParams.common.includeDeleted ?: false
                )
                .map { it.toModel() }
        )*/
    }


    override suspend fun updateLocal(
        list: List<Bookmark>,
        forceOverwrite: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidList(uids: List<String>): List<Bookmark> {
        TODO("Not yet implemented")
    }
}