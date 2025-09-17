package world.respect.datalayer.ext

import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.shared.LocalModelDataSource

@Suppress("unused")
suspend fun <T: Any> LocalModelDataSource<T>.updateFromRemoteListIfNeeded(
    remoteLoad: DataLoadState<List<T>>,
    validationHelper: ExtendedDataSourceValidationHelper?
) {
    if(remoteLoad is DataReadyState) {
        updateLocal(remoteLoad.data)
        validationHelper?.updateValidationInfo(remoteLoad.metaInfo)
    }
}

suspend fun <T: Any> LocalModelDataSource<T>.updateFromRemoteIfNeeded(
    remoteLoad: DataLoadState<T>,
    validationHelper: ExtendedDataSourceValidationHelper?
) {
    if(remoteLoad is DataReadyState) {
        updateLocal(listOf(remoteLoad.data))
        validationHelper?.updateValidationInfo(remoteLoad.metaInfo)
    }
}

