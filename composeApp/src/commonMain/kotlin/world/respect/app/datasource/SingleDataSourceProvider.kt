package world.respect.app.datasource

import world.respect.app.domain.account.RespectAccount
import world.respect.datasource.RespectAppDataSource

class SingleDataSourceProvider(
    private val datasource: RespectAppDataSource
): RespectAppDataSourceProvider {

    override fun getDataSource(account: RespectAccount): RespectAppDataSource {
        return datasource
    }
}