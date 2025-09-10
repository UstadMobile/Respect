package world.respect.datalayer.db.networkvalidation.entities

import androidx.room.Entity

/**
 * @property nviUrlHash XXHash64 of the URL
 * @property nviKey the validation info key as per ExtendedDataSourceValidationHelper.validationInfoKey
 */
@Entity(primaryKeys = ["nviUrlHash", "nviKey"])
data class NetworkValidationInfoEntity(
    val nviUrlHash: Long,
    val nviKey: Long,
    val nviVaryHeader: String?,
    val nviLastModified: Long,
    val nviEtag: String?,
    val nviConsistentThrough: Long,
    val nviLastChecked: Long,
)

