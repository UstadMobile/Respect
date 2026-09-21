package world.respect.datalayer.oneroster.model

/**
 * As per OneRoster spec 6.3.11:
 *
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Enumerated_RoleEnum
 */
enum class OneRosterRoleEnum(val value: String) {

    AIDE("aide"),
    COUNSELOR("counselor"),
    DISTRICT_ADMINISTRATOR("districtAdministrator"),
    GUARDIAN("guardian"),
    PARENT("parent"),
    PRINCIPAL("principal"),
    PROCTOR("proctor"),
    RELATIVE("relative"),
    SITE_ADMINISTRATOR("siteAdministrator"),
    STUDENT("student"),
    SYSTEM_ADMINISTRATOR("systemAdministrator"),
    TEACHER("teacher"),

}