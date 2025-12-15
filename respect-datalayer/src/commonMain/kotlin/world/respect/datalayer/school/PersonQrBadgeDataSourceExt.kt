package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.school.model.PersonQrCode
import world.respect.datalayer.shared.params.GetListCommonParams

fun PersonQrDataSource.findByPersonGuidAsFlow(
    guid: String
): Flow<DataLoadState<PersonQrCode>> {
    return listAllAsFlow(
        listParams = PersonQrDataSource.GetListParams(
            common = GetListCommonParams(guid = guid)
        )
    ).map {
        it.firstOrNotLoaded()
    }
}
