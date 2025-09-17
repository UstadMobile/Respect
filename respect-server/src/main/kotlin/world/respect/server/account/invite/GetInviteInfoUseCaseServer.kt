package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.libutil.util.throwable.withHttpStatusCode
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

class GetInviteInfoUseCaseServer(
    private val respectAppDb: RespectAppDatabase,
): GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): RespectInviteInfo {
        val serverDir = respectAppDb.getSchoolDirectoryEntityDao().getServerManagerSchoolDirectory()
        val codeWithoutDirectoryPrefix = code.substringAfter(serverDir?.rdInvitePrefix ?: "")
        val schoolDirectoryEntry = respectAppDb.getSchoolDirectoryEntryEntityDao()
            .findSchoolByInviteCode(codeWithoutDirectoryPrefix)
            ?: throw IllegalArgumentException("No school for code: $code").withHttpStatusCode(400)

        val schoolScopeId = SchoolDirectoryEntryScopeId(
            schoolDirectoryEntry.reSelf, null
        )
        val schoolScope = getKoin().getScope(schoolScopeId.scopeId)
        val schoolDb: RespectSchoolDatabase = schoolScope.get()

        val clazzInviteCode = codeWithoutDirectoryPrefix.substringAfter(
            schoolDirectoryEntry.reSchoolCode ?: ""
        )




        //Verify that this server handles the code prefix
        //Find the realm that handles the next prefix

        TODO("Use the school db obtained through Koin to find the class and the return the real RespectInviteInfo")
    }
}