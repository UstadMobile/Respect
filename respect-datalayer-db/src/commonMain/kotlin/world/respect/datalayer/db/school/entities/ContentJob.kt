package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents one job that the user created. One ContentJob can have one or more ContentJobItem(s),
 * each of which will have a source URI.
 *
 * Normally the ContentJob is local on any given node. It will however need to be replicated
 * in order to allow clients to see progress of an import that takes time to process on the server
 * (e.g. video import etc)
 *
 */
@Serializable
@Entity
data class ContentJob(

        @PrimaryKey(autoGenerate = true)
        var cjUid: Long = 0,

        //Where data should be saved (null = default device storage)
        var toUri: String? = null,

        var cjProgress: Long = 0,

        var cjTotal: Long = 0,

        var cjNotificationTitle: String? = null,

        var cjIsMeteredAllowed: Boolean = false,

        var params: String? = null,

        var cjLct: Long = 0,
) {
    companion object {
        const val TABLE_ID = 702
    }
}