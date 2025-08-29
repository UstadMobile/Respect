package world.respect.datalayer.oneroster.model

/**
 * As per OneRoster spec 6.3.7
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Enumerated_GenderEnum
 */
enum class OneRosterGenderEnum(val value: String) {

    FEMALE("female"), MALE("male"), OTHER("other"), UNSPECIFIED("unspecified")

}