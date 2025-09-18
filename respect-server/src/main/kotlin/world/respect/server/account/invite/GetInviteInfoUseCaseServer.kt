package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

class GetInviteInfoUseCaseServer(
    private val respectAppDb: RespectAppDatabase,
    private val respectAppDataSource: RespectAppDataSourceLocal,
): GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): RespectInviteInfo {
        val serverDir = respectAppDb.getSchoolDirectoryEntityDao().getServerManagerSchoolDirectory()
        val codeWithoutDirectoryPrefix = code.substringAfter(serverDir?.rdInvitePrefix ?: "")

        val directoryEntry = respectAppDataSource.schoolDirectoryEntryDataSource.list(
            DataLoadParams(), SchoolDirectoryEntryDataSource.GetListParams(
                code = codeWithoutDirectoryPrefix,
            )
        ).dataOrNull()?.firstOrNull()
            ?: throw IllegalArgumentException("No school for code: $code").withHttpStatus(400)

        val schoolScopeId = SchoolDirectoryEntryScopeId(directoryEntry.self, null)
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            schoolScopeId.scopeId
        )

        val schoolDb: RespectSchoolDatabase = schoolScope.get()
        println(schoolDb)

        val classInviteCode = codeWithoutDirectoryPrefix.substringAfter(
            directoryEntry.schoolCode ?: ""
        )

        TODO()
//        val classEntity = schoolDb.getClassEntityDao().findByInviteCode(classInviteCode)
//            ?: throw IllegalStateException("No class for code $code in school: ${schoolDirectoryEntry.reSelf}")
//
//        RespectInviteInfo(
//            code = code,
//            school = directoryEntry,
//            classGuid = classEntity.cGuid
//        )



        //Verify that this server handles the code prefix
        //Find the realm that handles the next prefix

        TODO("Use the school db obtained through Koin to find the class and the return the real RespectInviteInfo")
    }
}