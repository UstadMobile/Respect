package world.respect.datalayer.school.xapi

import world.respect.lib.xapi.resources.XapiResource

interface XapiResourceLocal: XapiResource {

    override val statements: XapiStatementsResourceLocal

    override val agents: XapiAgentsResourceLocal

    override val activities: XapiActivitiesResourceLocal

}