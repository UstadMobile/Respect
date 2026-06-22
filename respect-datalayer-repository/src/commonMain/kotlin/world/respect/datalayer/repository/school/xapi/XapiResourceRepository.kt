package world.respect.datalayer.repository.school.xapi

import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.xapi.XapiResourceLocal
import world.respect.lib.xapi.resources.XapiActivitiesResource
import world.respect.lib.xapi.resources.XapiAgentsResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStatementsResource

class XapiResourceRepository(
    private val local: XapiResourceLocal,
    private val remote: XapiResource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : XapiResource {


    override val statements: XapiStatementsResource by lazy {
        XapiStatementsResourceRepository(
            local = local.statements,
            remote = remote.statements,
            remoteWriteQueue = remoteWriteQueue,
        )
    }
    override val agents: XapiAgentsResource = local.agents

    override val activities: XapiActivitiesResource = local.activities

}