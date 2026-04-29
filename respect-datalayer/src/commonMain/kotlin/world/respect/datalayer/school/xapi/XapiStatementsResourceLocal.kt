package world.respect.datalayer.school.xapi

import world.respect.lib.xapi.model.XapiStatement
import world.respect.datalayer.shared.LocalModelDataSource
import world.respect.lib.xapi.resources.XapiStatementsResource

interface XapiStatementsResourceLocal: XapiStatementsResource, LocalModelDataSource<XapiStatement>
