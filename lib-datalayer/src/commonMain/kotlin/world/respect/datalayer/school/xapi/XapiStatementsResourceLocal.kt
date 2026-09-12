package world.respect.datalayer.school.xapi

import world.respect.lib.xapi.model.XapiStatement
import world.respect.datalayer.shared.LocalModelDataSource
import world.respect.lib.xapi.resources.XapiStatementsResource
import kotlin.uuid.Uuid

interface XapiStatementsResourceLocal: XapiStatementsResource, LocalModelDataSource<XapiStatement> {

    suspend fun getByUuid(uuid: Uuid): XapiStatement?

}
