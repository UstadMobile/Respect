package world.respect.datalayer.school.model.composites


/**
 * Data class for the details shown in the list (fewer fields than full entity; exists for performance
 * reasons)
 */
data class PersonListDetails(
    val guid: String,
    val givenName: String,
    val familyName: String,
    val username: String?
)
