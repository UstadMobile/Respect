package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.datalayer.shared.WritableDataSource

interface PersonPasskeyDataSource: WritableDataSource<PersonPasskey> {

    suspend fun listAll(): DataLoadState<List<PersonPasskey>>

    fun listAllAsFlow(): Flow<DataLoadState<List<PersonPasskey>>>


    companion object {

        const val ENDPOINT_NAME = "PersonPasskey"

    }

}