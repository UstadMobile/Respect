package world.respect.datalayer.oneroster.model

import com.eygraber.uri.Uri

/**
 * As per spec 6.1.16
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_GUIDRef
 */
sealed interface OneRosterGUIDRef {

    val href: Uri

    val sourcedId: String

}