package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
open class SiteTerms {

    @PrimaryKey(autoGenerate = true)
    var sTermsUid: Long = 0

    var termsHtml: String? = null

    //Two letter code for easier direct queries
    var sTermsLang: String? = null

    //Foreign key to the language object
    var sTermsLangUid: Long = 0

    var sTermsActive: Boolean = true

    var sTermsLastChangedBy: Int = 0

    var sTermsPrimaryCsn: Long = 0

    var sTermsLocalCsn: Long = 0

    var sTermsLct: Long = 0

    companion object {

        const val TABLE_ID = 272

    }

}