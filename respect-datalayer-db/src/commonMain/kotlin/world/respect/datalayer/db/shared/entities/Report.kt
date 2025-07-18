package world.respect.datalayer.db.shared.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * The Report entity is a little different: the data is not really stored as database fields, it is
 * kept in the reportOptions string as Json. We don't need to query or join using the reportoptions,
 * so there isn't a whole lot of point in keeping them as database fields. The ReportOptions are
 * deserialized and then used to generate SQL to run a report (see GenerateReportQueriesUseCase).
 *
 * @param reportOptions JSON string of ReportOptions2
 * @param
 */
@Entity
@Serializable
data class Report(
    @PrimaryKey(autoGenerate = true)
    var reportUid: Long = 0,

    var reportTitle: String? = null,

    var reportOptions: String? = null,

    var reportIsTemplate: Boolean = false,

    var reportLastModTime: Long = 0,

    @ColumnInfo(defaultValue = "0")
    var reportOwnerPersonUid: Long = 0,

) {
    companion object {
        const val TABLE_ID = 101
    }
}

