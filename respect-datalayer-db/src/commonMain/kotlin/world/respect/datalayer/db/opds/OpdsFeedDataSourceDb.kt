package world.respect.datalayer.db.opds

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSourceLocal
import world.respect.lib.opds.model.OpdsFeed

class OpdsFeedDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : OpdsFeedDataSourceLocal{

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: OpdsFeedDataSource.GetListParams
    ): List<OpdsFeed> {
        TODO("Run a query as normal")
    }

    override suspend fun store(list: List<OpdsFeed>) {
        //If any item is outside the school url, throw illegal argument exception

        TODO("Check permission, do processing/upsert")
    }

    override suspend fun updateLocal(
        list: List<OpdsFeed>,
        forceOverwrite: Boolean
    ) {
        TODO("Same as other entities")
    }

    override suspend fun findByUidList(uids: List<String>): List<OpdsFeed> {
        TODO("Consider list of uids ")
    }
}