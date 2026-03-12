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
enum class ChangeHistoryFieldEnum(val value: String) {

    PERSON_GIVEN_NAME("pGivenName"),
    PERSON_FAMILY_NAME("pFamilyName"),
    PERSON_MIDDLE_NAME("pMiddleName"),
    PERSON_USERNAME("pUsername"),
    PERSON_GENDER("pGender"),
    PERSON_EMAIL("pEmail"),
    PERSON_PHONE_NUMBER("pPhoneNumber"),
    PERSON_DATE_OF_BIRTH("pDateOfBirth"),

    CLASS_TITLE("cTitle"),
    CLASS_DESCRIPTION("cDescription"),
    CLASS_STATUS("cStatus"),


    ENROLLMENT_ROLE("eRole"),
    ENROLLMENT_BEGIN_DATE("eBeginDate"),
    ENROLLMENT_END_DATE("eEndDate"),
    ENROLLMENT_STATUS("eStatus");

    companion object {
        fun fromValue(value: String) =
            entries.first { it.value == value }
    }
}