package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.school.model.PersonQrBadge
import world.respect.datalayer.shared.params.GetListCommonParams

fun PersonQrBadgeDataSource.findByPersonGuidAsFlow(
    guid: String
): Flow<DataLoadState<PersonQrBadge>> {
    return listAllAsFlow(
        listParams = PersonQrBadgeDataSource.GetListParams(
            common = GetListCommonParams(
                guid = guid,
                includeDeleted = false,
            ),
        )
    ).map {
        it.firstOrNotLoaded()
    }
}
