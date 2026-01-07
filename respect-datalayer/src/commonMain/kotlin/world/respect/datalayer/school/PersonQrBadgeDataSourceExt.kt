package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.school.model.PersonBadge
import world.respect.datalayer.shared.params.GetListCommonParams

fun PersonQrDataSource.findByPersonGuidAsFlow(
    guid: String
): Flow<DataLoadState<PersonBadge>> {
    return listAllAsFlow(
        listParams = PersonQrDataSource.GetListParams(
            common = GetListCommonParams(guid = guid),
            includeDeleted = false
        )
    ).map {
        it.firstOrNotLoaded()
    }
}
