package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ErrorReport {

    @PrimaryKey(autoGenerate = true)
    var errUid: Long = 0

    var errPcsn: Long = 0

    var errLcsn: Long = 0

    var errLcb: Int = 0

    var errLct: Long = 0

    var severity: Int = 0

    var timestamp: Long = 0

    var presenterUri: String? = null

    var appVersion: String? = null

    var versionCode: Int = 0

    var errorCode: Int = 0

    var operatingSys: String? = null

    var osVersion: String? = null

    var stackTrace: String? = null

    var message: String? = null

}