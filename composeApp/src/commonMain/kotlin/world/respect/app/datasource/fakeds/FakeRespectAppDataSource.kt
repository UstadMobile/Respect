package world.respect.app.datasource.fakeds

import world.respect.datasource.RespectAppDataSource
import world.respect.datasource.compatibleapps.CompatibleAppsDataSource
import world.respect.datasource.opds.OpdsDataSource

class FakeRespectAppDataSource: RespectAppDataSource {

    override val compatibleAppsDataSource: CompatibleAppsDataSource = FakeAppDataSource()

    override val opdsDataSource: OpdsDataSource = FakeOpdsDataSource()

}