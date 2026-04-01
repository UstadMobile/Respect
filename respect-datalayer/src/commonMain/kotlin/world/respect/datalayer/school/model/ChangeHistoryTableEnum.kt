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

    PERSON_GIVEN_NAME("pGivenName", "Person name"),
    PERSON_FAMILY_NAME("pFamilyName", "Person name"),
    PERSON_MIDDLE_NAME("pMiddleName", "Person name"),
    PERSON_USERNAME("pUsername", "Username"),
    PERSON_GENDER("pGender", "Gender"),
    PERSON_EMAIL("pEmail", "Email"),
    PERSON_PHONE_NUMBER("pPhoneNumber", "Phone Number"),
    PERSON_DATE_OF_BIRTH("pDateOfBirth", "date of birth"),

    CLASS_STATUS("cStatus", "Class Status"),
    CLASS_TITLE("cTitle", "Class Title"),
    CLASS_DESCRIPTION("cDescription", "Class Description"),
    CLASS_TEACHER_ADDED("cTeacherAdded", "Teacher Added"),
    CLASS_TEACHER_REMOVED("cTeacherRemoved", "Teacher Removed"),
    CLASS_STUDENT_ADDED("cStudentAdded", "Student Added"),
    CLASS_STUDENT_REMOVED("cStudentRemoved", "Student Removed"),
    JOIN_REQUEST_APPROVED("joinRequestApproved", "Join request approved for"),
    JOIN_REQUEST_REJECTED("joinRequestRejected", "Join request rejected for"),
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