package world.respect.datalayer.school.xapi

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.lib.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiGroup

interface XapiActorDataSource {

    suspend fun getPerson(
        actor: XapiActor,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    )

    suspend fun getGroupDetail(groupId: String): XapiGroup?

    suspend fun getGroupsByIds(groupIds: List<String>): List<XapiGroup>

    fun getGroupDetailAsFlow(groupId: String): Flow<XapiGroup?>


}