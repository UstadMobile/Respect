package world.respect.server.account.deleteaccount

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.deleteaccount.DeleteAccountUseCase

class DeleteAccountUseCaseServer(
    private val schoolDb: RespectSchoolDatabase
) : DeleteAccountUseCase {

    override suspend fun invoke(guid: String): Boolean {
        return try {
            val guidHash = guid.toLong()   // DB expects Long hash
            val rows = schoolDb
                .getPersonEntityDao()
                .deleteByPersonGuidHash(guidHash)

            rows > 0
        } catch (e: Exception) {
            throw IllegalStateException("DeleteAccountUseCase failed for guid=$guid: ${e.message}")
                .withHttpStatus(500)
        }
    }
}
