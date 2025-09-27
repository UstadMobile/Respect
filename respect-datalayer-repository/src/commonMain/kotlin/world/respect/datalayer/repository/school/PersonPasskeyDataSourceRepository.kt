package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.PersonPasskeyDataSourceLocal
import world.respect.datalayer.school.model.PersonPasskey

class PersonPasskeyDataSourceRepository(
    val local: PersonPasskeyDataSourceLocal,
    val remote: PersonPasskeyDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
): PersonPasskeyDataSource {

    override suspend fun listAll(): DataLoadState<List<PersonPasskey>> {
        val remoteResult = remote.listAll()
        local.updateFromRemoteListIfNeeded(remoteResult, validationHelper)

        return local.listAll()
    }

    override fun listAllAsFlow(): Flow<DataLoadState<List<PersonPasskey>>> {
        return local.listAllAsFlow().combineWithRemote(
            remoteFlow = remote.listAllAsFlow().onEach {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun store(list: List<PersonPasskey>) {
        TODO("Not yet implemented")
    }
}