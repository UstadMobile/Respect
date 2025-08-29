package world.respect.datalayer.db.schooldirectory

import androidx.room.Transactor
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.schooldirectory.adapters.SchoolDirectoryEntryEntities
import world.respect.datalayer.db.schooldirectory.adapters.toEntities
import world.respect.datalayer.db.schooldirectory.adapters.toModel
import world.respect.datalayer.db.schooldirectory.entities.SchoolConfigEntity
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.libxxhash.XXStringHasher
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SchoolDirectoryDataSourceDb(
    private val respectAppDb: RespectAppDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
): SchoolDirectoryDataSourceLocal {

    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun getServerManagedDirectory(): RespectSchoolDirectory? {
        TODO()
    }

    override suspend fun addServerManagedSchool(
        school: SchoolDirectoryEntry,
        dbUrl: String,
    ) {
        respectAppDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                upsertSchoolDirectoryEntry(
                    school = DataReadyState(
                        school,
                        DataLoadMetaInfo(
                            lastModified = Clock.System.now().toEpochMilliseconds()
                        )
                    ),
                    directory = null
                )

                respectAppDb.getSchoolConfigEntityDao().upsert(
                    SchoolConfigEntity(
                        rcUid = xxStringHasher.hash(school.self.toString()),
                        dbUrl = dbUrl,
                    )
                )
            }
        }
    }

    override suspend fun getSchoolDirectoryEntryByUrl(url: Url): DataReadyState<SchoolDirectoryEntry>? {
        val schoolEntity = respectAppDb.getSchoolEntityDao().findByUid(
            xxStringHasher.hash(url.toString())
        )

        if(schoolEntity == null)
            return null

        val langMapEntities = respectAppDb.getLangMapEntityDao().selectAllByTableAndEntityId(
            lmeTopParentType = LangMapEntity.TopParentType.RESPECT_SCHOOL_DIRECTORY_ENTRY.id,
            lmeEntityUid1 = schoolEntity.reUid,
            lmeEntityUid2 = 0L
        )

        return SchoolDirectoryEntryEntities(
            school = schoolEntity,
            langMapEntities = langMapEntities
        ).toModel()
    }

    override suspend fun upsertSchoolDirectoryEntry(
        school: DataReadyState<SchoolDirectoryEntry>,
        directory: RespectSchoolDirectory?
    ) {
        val schoolUid = xxStringHasher.hash(school.data.self.toString())

        respectAppDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                respectAppDb.getLangMapEntityDao().deleteByTableAndTopParentType(
                    lmeTopParentType = LangMapEntity.TopParentType.RESPECT_SCHOOL_DIRECTORY_ENTRY.id,
                    lmeEntityUid1 = schoolUid,
                )

                val schoolEntities = school.toEntities(xxStringHasher)
                respectAppDb.getSchoolEntityDao().upsert(schoolEntities.school)
                respectAppDb.getLangMapEntityDao().insertAsync(schoolEntities.langMapEntities)
            }
        }
    }

    override suspend fun allSchoolsInDirectory(): List<SchoolDirectoryEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun searchSchools(text: String): Flow<DataLoadState<List<SchoolDirectoryEntry>>> = flow {
        emit(DataLoadingState())

        try {
            val result = respectAppDb.useReaderConnection { con ->
                con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                    val langMapList = respectAppDb.getLangMapEntityDao().searchByLmeValue(
                        value = text,
                        topParentType = LangMapEntity.TopParentType.RESPECT_SCHOOL_DIRECTORY_ENTRY.id,
                        propType = LangMapEntity.PropType.RESPECT_SCHOOL_DIRECTORY_ENTRY_NAME.id

                    )
                    langMapList.mapNotNull { langMapEntity ->
                        val schoolDirectoryEntryEntity = respectAppDb.getSchoolEntityDao().findByUid(
                            langMapEntity.lmeTopParentUid1
                        )
                        if (schoolDirectoryEntryEntity != null) {
                            SchoolDirectoryEntryEntities(
                                school = schoolDirectoryEntryEntity,
                                langMapEntities = langMapList
                            ).toModel().data
                        } else {
                            null
                        }
                    }
                }
            }
            emit(DataReadyState(result))
        } catch (e: Throwable) {
            emit(DataErrorResult(e))
        }
    }

    override suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo {
        TODO("Not yet implemented")
    }
}