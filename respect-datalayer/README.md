# RESPECT-Datalayer

The designed is based on the [Android offline-first data layer architecture](https://developer.android.com/topic/architecture/data-layer/offline-first)
and is derived from the implementation of [UstadMobile](https://www.github.com/UstadMobile/UstadMobile/)
and to some extent [Door](https://www.github.com/UstadMobile/door/). 

A data source implementation can be:
 * [Local](../respect-datalayer-db/) - e.g. using a Room database
 * [Network](../respect-datalayer-http/) - using HTTP over a REST API
 * [Repository](../respect-datalayer-repository/) - an offline-first combination of local and network data sources that mediates 
   a local and network datasource such that when:
    * **Reading**: locally available data is loaded and can be displayed to the user immediately 
      whilst checking for updates in the background. Caching validation (etag, if-modified-since) 
      is used to conserve bandwidth. 
    * **Write**: updated data is written to the local data source immediately and then enqueued to 
      be written to the remote data source as soon as a connection is available.

Where a datasource is being used to access data that requires authorization (e.g. school level data
as per [ARCHITECTURE.md](../ARCHITECTURE.md)) that datasource MUST be tied to an authenticated user
and enforce permissions.

The ViewModel sees only the interface, and does not need to be concerned with the underlying 
implementation.

This helps maximize code reusage:
 * Desktop/mobile app: uses the offline-first repository implementation as the datasource.
 * Browser app: uses only the network implementation on its own as the datasource (no local database).
 * Server app: uses only the local (database based) implementation on its own as the datasource, 
   primarily to serve REST API endpoints.

