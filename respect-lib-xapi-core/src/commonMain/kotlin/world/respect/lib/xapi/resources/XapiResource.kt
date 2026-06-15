package world.respect.lib.xapi.resources

interface XapiResource {

    val statements: XapiStatementsResource

    val agents: XapiAgentsResource

    val activities: XapiActivitiesResource

}