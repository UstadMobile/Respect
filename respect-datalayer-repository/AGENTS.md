* This module contains an offline-first repository implementation of the datalayer. It uses the
  respect-datalayer-db module as the local datalayer and respect-datalayer-http as the remote 
  datalayer
* Always follow the existing patterns used in this module unless explicitly told otherwise.
* Any writable DataSourceRepository implementation must always implement  the
  ```world.respect.datalayer.repository.school.RepositoryModelDataSource``` interface.
* Read functions should be implemented using the patterns seen in existing DataSources e.g.
  ```src/commonMain/kotlin/world/respect/datalayer/repository/school/AssignmentDataSourceRepository.kt```
  and ```respect-datalayer-repository/src/commonMain/kotlin/world/respect/datalayer/repository/school/ClassDataSourceRepository.kt``` 
* The store function should be implemented using the RemoteWriteQueue as per existing patterns. When
  adding a new repository add the model type to the Enum class ```world.respect.datalayer.school.writequeue.WriteQueueItem.Model```
  and update the invoke function in ```world.respect.datalayer.repository.school.writequeue.DrainRemoteWriteQueueUseCase```
  to support that data type.
