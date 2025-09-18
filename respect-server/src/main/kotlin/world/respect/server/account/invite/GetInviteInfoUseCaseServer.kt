package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.entities.ClassEntity
import world.respect.datalayer.db.schooldirectory.adapters.SchoolDirectoryEntryEntities
import world.respect.datalayer.db.schooldirectory.adapters.toModel
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.libutil.util.throwable.withHttpStatusCode
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.viewmodel.app.appstate.getTitle

class GetInviteInfoUseCaseServer(
    private val respectAppDb: RespectAppDatabase,
): GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): RespectInviteInfo {
        val serverDir = respectAppDb.getSchoolDirectoryEntityDao().getServerManagerSchoolDirectory()
        val codeWithoutDirectoryPrefix = code.substringAfter(serverDir?.rdInvitePrefix ?: "")
        val schoolDirectoryEntryEntity = respectAppDb.getSchoolDirectoryEntryEntityDao()
            .findSchoolByInviteCode(codeWithoutDirectoryPrefix)
            ?: throw IllegalArgumentException("No school for code: $code $codeWithoutDirectoryPrefix $serverDir").withHttpStatusCode(400)

        val schoolScopeId = SchoolDirectoryEntryScopeId(schoolDirectoryEntryEntity.reSelf, null)
        val schoolScope = getKoin().getScope(schoolScopeId.scopeId)
        val schoolDb: RespectSchoolDatabase = schoolScope.get()

        val clazzInviteCode = codeWithoutDirectoryPrefix.substringAfter(
            schoolDirectoryEntryEntity.reSchoolCode ?: ""
        )
        val langMapEntities = respectAppDb.getLangMapEntityDao().findAllByFeedUid(schoolDirectoryEntryEntity.reUid)
        val schoolDirectoryEntry = SchoolDirectoryEntryEntities(
            school = schoolDirectoryEntryEntity,
            langMapEntities = langMapEntities
        ).toModel()
        val classDao = schoolDb.getClassEntityDao()
        val teacherClass = classDao.findByTeacherInviteCode(clazzInviteCode)
        val studentClass = classDao.findByStudentInviteCode(clazzInviteCode)

        val clazz: ClassEntity
        val inviteType: RespectInviteInfo.UserInviteType

        when {
            teacherClass != null -> {
                clazz = teacherClass
                inviteType = RespectInviteInfo.UserInviteType.TEACHER
            }
            studentClass != null -> {
                clazz = studentClass
                inviteType = RespectInviteInfo.UserInviteType.STUDENT_OR_PARENT
            }
            else -> throw IllegalArgumentException("No class for code: $code").withHttpStatusCode(400)
        }

        return RespectInviteInfo(
            code = code,
            school = schoolDirectoryEntry.data,
            classGuid = clazz.cGuid,
            className = clazz.cTitle,
            schoolName = schoolDirectoryEntry.data.name.getTitle(),
            userInviteType = inviteType
        )
    }
}