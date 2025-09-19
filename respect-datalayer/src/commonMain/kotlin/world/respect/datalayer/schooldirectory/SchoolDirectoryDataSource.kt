package world.respect.datalayer.schooldirectory

import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.invite.RespectInviteInfo

/**
 * DataSource to access all known directories
 */
interface SchoolDirectoryDataSource {

    suspend fun allDirectories(): List<RespectSchoolDirectory>

    suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo


}