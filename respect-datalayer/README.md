# RESPECT-Datalayer

The designed is based on the [Android offline-first data layer architecture](https://developer.android.com/topic/architecture/data-layer/offline-first)
and is derived from the implementation of [UstadMobile](https://www.github.com/UstadMobile/UstadMobile/)
and to some extent [Door](https://www.github.com/UstadMobile/door/). 

A data source implementation can be:
 * [Local](../respect-datalayer-db/) - e.g. using a Room database
 * [Network](../respect-datalayer-http/) - using HTTP over a REST API
 * [Repository](../respect-datalayer-repository/) - an offline-first combination of local and network 
   data sources that mediates a local and network datasource.

Where a datasource is being used to access data that requires authorization (e.g. school level data
as per [ARCHITECTURE.md](../ARCHITECTURE.md)) that datasource MUST be tied to an authenticated user
and enforce permissions.

The ViewModel sees only the interface, and does not need to be concerned with the underlying 
implementation.

This helps maximize code reusage:
 * Desktop/mobile apps: uses the offline-first repository implementation as the datasource 
 * Browser app: uses the network implementation on its own as the datasource (no local database)
 * Server app: uses the local (database based) implementation on its own as the datasource, primarily 
   to server REST API endpoints.

