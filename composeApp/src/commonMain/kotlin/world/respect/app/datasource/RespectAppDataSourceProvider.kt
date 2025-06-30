package world.respect.app.datasource

import world.respect.app.domain.account.RespectAccount
import world.respect.datasource.RespectAppDataSource

/**
 * A RespectAppDataSource is tied to one (user) account.
 *
 * If a "Learning Space" (as per the Ustad Concept) provides information on permission grants, the
 * underlying SQL database can be shared between multiple accounts (e.g.
 * class RespectAppDataSourceDb(
 *    val db: Database, //shared if permission info provided
 *    val accountId: String
 * ) {
 *    //In datasource functions
 *
 *    fun getSomeData(): Flow<DataLoadState<SomeData>> {
 *       return db.dao.getSomeDataAsFlow(accountId)
 *    }
 * }
 */
interface RespectAppDataSourceProvider {

    /**
     *
     */
    fun getDataSource(account: RespectAccount): RespectAppDataSource

}
