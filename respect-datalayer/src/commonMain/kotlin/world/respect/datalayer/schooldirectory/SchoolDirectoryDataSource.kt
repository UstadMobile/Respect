package world.respect.datalayer.schooldirectory

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.school.ClassDataSource.GetListParams
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.shared.paging.IPagingSourceFactory

/**
 * DataSource to access all known directories
 */
interface SchoolDirectoryDataSource {

    suspend fun allDirectories(): List<RespectSchoolDirectory>

    suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo

}