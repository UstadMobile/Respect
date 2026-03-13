package world.respect.datalayer.school.opds

import world.respect.datalayer.DataReadyState
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.lib.opds.model.OpdsPublication

interface OpdsPublicationDataSourceLocal: OpdsPublicationDataSource {

    val publicationNetworkValidationHelper: BaseDataSourceValidationHelper

    suspend fun updateOpdsPublication(publication: DataReadyState<OpdsPublication>)

}