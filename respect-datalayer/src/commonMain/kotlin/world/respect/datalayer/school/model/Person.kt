package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * @property guid unique system identifier. As per various specs it MUST NOT contain personal
 *           information (e.g. username, name, etc). It could be a sequential key, etc.
 * @property userMasterIdentifier generally an ID assigned by the organization (e.g. government
 *           issued student/teacher id number)
 * @property userActive (should be changed to status enum - which may be pending)
 */

@Serializable
data class Person(
    val guid: String,
    val userActive: Boolean = true,
    val status: StatusEnum = StatusEnum.ACTIVE,
    @Serializable(with = InstantISO8601Serializer::class)
    override val lastModified: Instant = Clock.System.now(),
    @Serializable(with = InstantISO8601Serializer::class)
    override val stored: Instant = Clock.System.now(),
    val userMasterIdentifier: String? = null,
    val username: String? = null,
    val givenName: String,
    val familyName: String,
    val middleName: String? = null,
    val preferredFirstName: String? = null,
    val preferredMiddleName: String? = null,
    val preferredLastName: String? = null,
    val pronouns: String? = null,
    val roles: List<PersonRole>,
): ModelWithTimes {

    companion object {
        const val TABLE_ID = 2
    }

}