# RESPECT respect-datalayer-repository guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview
This is an implementation of the [respect-datalayer module](../respect-datalayer)
that provides an offline-first repository combining a local datasource (respect-datalayer-db)
and a remote datasource (e.g. respect-datalayer-http). It works as follows:

* **Offline-first read**: checks the local datasource first, and when used as a flow, immediately returns the
  local data without waiting for the remote data. The remote data is checked asynchronously if
  connectivity is available. If new remote data is available, the updateLocal function is used to
  update the local data and the flow is updated.
* **Offline-first write**: updates the local data immediately and enqueues the remote update to the 
  `RemoteWriteQueue`. The RemoteWriteQueue will be drained by DrainRemoteWriteQueueUseCase, which is
  handled using WorkManager on Android (using the connectivity constraint so it will run as soon
  as connectivity is available).

You should read [respect-datalayer AGENTS.md](../respect-datalayer/AGENTS.md) before working with
code in this module.

* Always follow the existing patterns used in this module unless explicitly told otherwise.
* The store function should be implemented using the RemoteWriteQueue as per existing patterns. When
  adding a new repository add the model type to the Enum class ```world.respect.datalayer.school.writequeue.WriteQueueItem.Model```
  and update the invoke function in ```world.respect.datalayer.repository.school.writequeue.DrainRemoteWriteQueueUseCase```
  to support that data type.
