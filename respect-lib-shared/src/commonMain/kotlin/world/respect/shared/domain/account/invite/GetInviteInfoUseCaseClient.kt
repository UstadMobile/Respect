package world.respect.shared.domain.account.invite

import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource

class GetInviteInfoUseCaseClient(
    private val schoolDirectoryDataSource: SchoolDirectoryDataSource
): GetInviteInfoUseCase {

    override suspend fun invoke(code: String): RespectInviteInfo {
        return schoolDirectoryDataSource.getInviteInfo(code)
    }
}
