package world.respect.datalayer.oneroster.model

/**
 * As per OneRoster spec 6.3.2
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#TabEnumeratedClass_DataModel_BaseStatusEnum
 */
enum class OneRosterBaseStatusEnum(val value: String) {

    ACTIVE("active"), TO_BE_DELETED("tobedeleted")
}