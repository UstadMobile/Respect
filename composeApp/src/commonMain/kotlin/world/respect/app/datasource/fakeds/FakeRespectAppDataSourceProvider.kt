package world.respect.app.datasource.fakeds

import world.respect.app.datasource.RespectAppDataSourceProvider
import world.respect.app.domain.account.RespectAccount
import world.respect.datasource.RespectAppDataSource

class FakeRespectAppDataSourceProvider: RespectAppDataSourceProvider {

    override fun getDataSource(account: RespectAccount): RespectAppDataSource {
        return FakeRespectAppDataSource()
    }

}