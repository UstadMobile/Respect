package world.respect.datalayer.school.xapi

import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.shared.LocalModelDataSource

interface  XapiStatementDataSourceLocal: XapiStatementDataSource, LocalModelDataSource<XapiStatement>
