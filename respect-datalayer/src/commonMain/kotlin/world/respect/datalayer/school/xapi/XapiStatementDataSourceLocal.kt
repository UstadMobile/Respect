package world.respect.datalayer.school.xapi

import world.respect.lib.xapi.model.XapiStatement
import world.respect.datalayer.shared.LocalModelDataSource

interface XapiStatementDataSourceLocal: XapiStatementDataSource, LocalModelDataSource<XapiStatement>
