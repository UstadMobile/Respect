package world.respect.datalayer.db.schooldirectory

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.serialization.json.Json
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.schooldirectory.entities.SchoolConfigEntity
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.libxxhash.XXStringHasher

class SchoolDirectoryDataSourceDb(
    private val respectAppDb: RespectAppDatabase,
    private val json: Json,
    private val xxStringHasher: XXStringHasher,
): SchoolDirectoryDataSourceLocal {

    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        return respectAppDb.getSchoolDirectoryEntityDao().getSchoolDirectories().map { schoolDirectory ->
            RespectSchoolDirectory(
                invitePrefix = schoolDirectory.rdInvitePrefix,
                baseUrl = schoolDirectory.rdUrl
            )
        }
    }


    override suspend fun getDirectoryByInviteCode(code: String): RespectSchoolDirectory? {
        return respectAppDb.getSchoolDirectoryEntityDao().getSchoolDirectoryByInviteCode(code)?.let {
            RespectSchoolDirectory(it.rdInvitePrefix, it.rdUrl)
        }
    }

    override suspend fun getServerManagedDirectory(): RespectSchoolDirectory? {
        return respectAppDb.getSchoolDirectoryEntityDao().getServerManagerSchoolDirectory()?.let {
            RespectSchoolDirectory(it.rdInvitePrefix, it.rdUrl)
        }
    }

    override suspend fun setServerManagedSchoolConfig(
        school: SchoolDirectoryEntry,
        dbUrl: String,
    ) {
        respectAppDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                respectAppDb.getSchoolConfigEntityDao().upsert(
                    SchoolConfigEntity(
                        rcUid = xxStringHasher.hash(school.self.toString()),
                        dbUrl = dbUrl,
                    )
                )
            }
        }
    }

    override suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDirectory(directory: RespectSchoolDirectory) {
        respectAppDb.getSchoolDirectoryEntityDao().deleteByUrl(directory.baseUrl.toString())
    }

}