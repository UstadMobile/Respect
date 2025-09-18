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
            loadParams = DataLoadParams(),
            listParams = SchoolDirectoryEntryDataSource.GetListParams(
                code = codeWithoutDirectoryPrefix,
            )
        ).dataOrNull()?.firstOrNull()
            ?: throw IllegalArgumentException("No school for code: $code").withHttpStatus(404)

        val schoolScopeId = SchoolDirectoryEntryScopeId(
            directoryEntry.self, null
        )

        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            schoolScopeId.scopeId
        )

        //Need to use the database because we don't have an authenticated user to get account scope
        val schoolDb: RespectSchoolDatabase = schoolScope.get()
        val classInviteCode = codeWithoutDirectoryPrefix.substringAfter(
            directoryEntry.schoolCode ?: ""
        )

        val clazz = schoolDb.getClassEntityDao().list(
            code = classInviteCode
        ).firstOrNull() ?: throw IllegalArgumentException("class not found for code: $code").withHttpStatus(404)

        return RespectInviteInfo(
            code = code,
            school = directoryEntry,
            classGuid = clazz.cGuid,
            className = clazz.cTitle,
            userInviteType = if(classInviteCode == clazz.cTeacherInviteCode) {
                RespectInviteInfo.UserInviteType.TEACHER
            }else {
                RespectInviteInfo.UserInviteType.STUDENT_OR_PARENT
            }
        )
    }
}