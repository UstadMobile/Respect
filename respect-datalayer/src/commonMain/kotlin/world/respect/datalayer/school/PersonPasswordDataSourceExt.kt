package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.school.model.PersonPassword
import world.respect.datalayer.shared.params.GetListCommonParams

fun PersonPasswordDataSource.findByPersonGuidAsFlow(
    guid: String
): Flow<DataLoadState<PersonPassword>> {
    return listAllAsFlow(
        listParams = PersonPasswordDataSource.GetListParams(
            common = GetListCommonParams(guid = guid)
        )
    ).map {
        it.firstOrNotLoaded()
    }
}
