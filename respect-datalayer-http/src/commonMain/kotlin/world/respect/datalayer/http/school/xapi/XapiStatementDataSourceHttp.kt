package world.respect.datalayer.http.school.xapi

import world.respect.datalayer.school.xapi.XapiStatementDataSource
import world.respect.lib.xapi.model.XapiStatement
import kotlin.uuid.Uuid

class XapiStatementDataSourceHttp: XapiStatementDataSource {

    override suspend fun post(list: List<XapiStatement>): List<Uuid> {
        TODO("Not yet implemented")
    }

    override suspend fun get(request: XapiStatementDataSource.GetStatementsRequest): XapiStatementDataSource.GetStatementsResponse {
        TODO("Not yet implemented")
    }
}