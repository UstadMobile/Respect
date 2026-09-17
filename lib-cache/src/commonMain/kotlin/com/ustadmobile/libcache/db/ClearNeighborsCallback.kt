package com.ustadmobile.libcache.db

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * When database is opened (e.g. app has restarted) any previously known neighbors should be cleared
 */
class ClearNeighborsCallback: RoomDatabase.Callback() {

    override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("DELETE FROM NeighborCache")
    }

}