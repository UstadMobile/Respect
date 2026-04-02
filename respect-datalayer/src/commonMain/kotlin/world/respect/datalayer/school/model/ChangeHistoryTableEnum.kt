package world.respect.datalayer.school.model


enum class ChangeHistoryTableEnum(val value: String) {

    PERSON("person"),
    CLASS("class"),
    ENROLLMENT("enrollment");

    companion object {
        fun fromValue(value: String) =
            entries.first { it.value == value }
    }
}
enum class ChangeHistoryFieldEnum(
    val value: String,
    val displayName: String
) {

    PERSON_GIVEN_NAME("pGivenName", "First Name"),
    PERSON_FAMILY_NAME("pFamilyName", "Last Name"),
    PERSON_MIDDLE_NAME("pMiddleName", "Middle Name"),
    PERSON_USERNAME("pUsername", "Username"),
    PERSON_GENDER("pGender", "Gender"),
    PERSON_EMAIL("pEmail", "Email"),
    PERSON_PHONE_NUMBER("pPhoneNumber", "Phone Number"),
    PERSON_DATE_OF_BIRTH("pDateOfBirth", "Date of Birth"),

    CLASS_TITLE("cTitle", "Class Title"),
    CLASS_DESCRIPTION("cDescription", "Class Description"),
    CLASS_STATUS("cStatus", "Class Status"),

    ENROLLMENT_ROLE("eRole", "Role"),
    ENROLLMENT_BEGIN_DATE("eBeginDate", "Start Date"),
    ENROLLMENT_END_DATE("eEndDate", "End Date"),
    ENROLLMENT_STATUS("eStatus", "Status");

    companion object {

        fun fromValue(value: String): ChangeHistoryFieldEnum {
            return entries.first { it.value == value }
        }

        fun fromValueOrNull(value: String): ChangeHistoryFieldEnum? {
            return entries.find { it.value == value }
        }
    }
}