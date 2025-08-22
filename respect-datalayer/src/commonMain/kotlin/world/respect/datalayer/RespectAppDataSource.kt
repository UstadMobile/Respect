package world.respect.datalayer

import world.respect.datalayer.compatibleapps.CompatibleAppsDataSource
import world.respect.datalayer.opds.OpdsDataSource
import world.respect.datalayer.realmdirectory.RealmDirectoryDataSource

/**
 * DataSource that provides app (eg. system) level data that is NOT specific to a given realm (eg
 * school - see ARCHITECTURE.md for more info).
 *
 * This includes:
 * a) RESPECT Compatible app manifests (loaded over http, cached locally, loaded offline-first)
 * b) OPDS feeds (loaded over http, cached locally, loaded offline-first)
 * c) Directory of available realms:
 *  i) On the server this is the list of Realms run on that given server
 *  ii) On the client this is the list of known directory servers and realms as listed by those servers
 *
 */
interface RespectAppDataSource {

    val compatibleAppsDataSource: CompatibleAppsDataSource

    val opdsDataSource: OpdsDataSource

    val realmDirectoryDataSource: RealmDirectoryDataSource

}
