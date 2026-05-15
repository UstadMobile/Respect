package world.respect.server.account.invite.clazz

import org.koin.core.component.KoinComponent
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.child.GetClassUseCase

class GetClassUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper
) : GetClassUseCase, KoinComponent {


    override suspend operator fun invoke(classUid: String): String {
        return schoolDb.getClassEntityDao().findByGuid(uidNumberMapper(classUid))?.clazz?.cTitle
            ?: throw IllegalArgumentException("No class found").withHttpStatus(404)

    }
}
