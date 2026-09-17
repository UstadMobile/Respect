# RESPECT-Datalayer-Db

This provides an implementation of the datalayer based on a Room database. It is used by the http
server to provide an implementation for REST API endpoints and by mobile/desktop client apps to
provide a local datasource used by the offline-first [repository](../respect-datalayer-repository/).

The School datalayer is tied to a specific user account and will enforce permissions (hence 
permission enforcement is done both on the HTTP server itself and on the local client).
