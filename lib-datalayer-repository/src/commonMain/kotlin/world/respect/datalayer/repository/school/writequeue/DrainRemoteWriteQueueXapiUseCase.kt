package world.respect.datalayer.repository.school.writequeue

import kotlinx.serialization.json.Json
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.writequeue.RemoteWriteQueue

class DrainRemoteWriteQueueXapiUseCase(
    private val remoteWriteQueue: RemoteWriteQueue,
    private val dataSource: SchoolDataSource,
    private val json: Json,
) {


}