package world.respect.callback

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import world.respect.libxxhash.XXStringHasher
import java.util.Properties

class AddSchoolDirectoryCallback(
    private val xxStringHasher: XXStringHasher,
) : RoomDatabase.Callback() {

    override fun onOpen(connection: SQLiteConnection) {
        val props = Properties()

        val defaultStream = javaClass.classLoader
            ?.getResourceAsStream("directories/default.properties")
            ?: throw IllegalStateException("default.properties not found in resources")
        props.load(defaultStream)

        val localStream = javaClass.classLoader
            ?.getResourceAsStream("directories/local.properties")
        localStream?.use { props.load(it) }
        if (localStream != null) {
            props.load(localStream)
        }

        props.forEach { key, value ->
            val url = value.toString()
            val uid = xxStringHasher.hash(url)
            val prefix = key.toString()

            connection.execSQL(
                """
                INSERT OR IGNORE INTO SchoolDirectoryEntity(rdUid, rdUrl, rdInvitePrefix) 
                VALUES('$uid','$url','$prefix')
                """.trimIndent()
            )
        }
    }
}
