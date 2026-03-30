package world.respect.datalayer.http.school.xapi

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.xapi.XapiStatementDataSource
import world.respect.datalayer.school.xapi.model.XapiStatement

class XapiStatementDataSourceHttp: XapiStatementDataSource {

    override suspend fun store(list: List<XapiStatement>) {
        TODO("Not yet implemented")
    }

    override suspend fun list(
        listParams: XapiStatementDataSource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<XapiStatement>> {
        TODO("Not yet implemented")
    }
}