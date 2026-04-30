package world.respect.datalayer.school.model

data class PersonWithEnrollment(
    val person: Person,
    val clazz : Clazz,
    val enrollment: Enrollment
)