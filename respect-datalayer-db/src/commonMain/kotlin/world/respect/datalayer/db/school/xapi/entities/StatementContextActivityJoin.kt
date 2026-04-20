package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
/**
 *
 *
 * @param scajFromStatementIdHi the most significant bits of the statement uuid
 * @param scajFromStatementIdLo the least significant bits of the statement uuid
 * @param scajContextType Integer flag based on the contextActivity property e.g. parent, grouping,
 * category, or other
 * @param scajToActivityId the IRI id of the activity that is being referenced
 * @param scajToActivityUid for key that joins to the activity (ActivityEntity.actUid)
 */
data class StatementContextActivityJoin(
    @PrimaryKey(autoGenerate = true)
    val scajUid: Long = 0,
    val scajFromStatementIdHi: Long,
    val scajFromStatementIdLo: Long,
    val scajContextType: StatementContextActivityJoinTypeEnum,
    val scajToActivityUid: Long,
    val scajToActivityId: String,
)
