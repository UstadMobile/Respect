package world.respect.datasource.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CompatibleAppEntity(
    @PrimaryKey
    val caeUid: Long,
    val caeUrl: String,
    val caeLastModified: Long,
    val caeEtag: String?,
    val caeLicense: String,
    val caeWebsite: String,
    val caeLearningUnits: String,
    val caeDefaultLaunchUri: String,
    val caeAndroidPackageId: String?,
    val caeAndroidStoreList: String?,
    val caeAndroidSourceCode: String?,
) {

    companion object {

        const val TABLE_ID = 42

        const val LANGMAP_PROP_NAME = 1L

        const val LANGMAP_PROP_DESC = 2L

    }

}

