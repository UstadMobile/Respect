package world.respect.datalayer.oneroster.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * See OneRoster 1.2 spec section 6.1.30
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_User
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class OneRosterUser(
    override val sourcedId: String,
    override val status: OneRosterBaseStatusEnum = OneRosterBaseStatusEnum.ACTIVE,
    @Serializable(with = InstantISO8601Serializer::class)
    override val dateLastModified: Instant,
    override val metadata: JsonObject? = null,
    val userMasterIdentifier: String? = null,
    val username: String? = null,
    val userIds: List<OneRosterUserId>?= null,
    val enabledUser: Boolean = true,
    val givenName: String,
    val familyName: String,
    val middleName: String? = null,
    val preferredFirstName: String? = null,
    val preferredMiddleName: String? = null,
    val preferredLastName: String? = null,
    val pronouns: String? = null,
    val roles: List<OneRosterRole>,
    val userProfiles: List<OneRosterUserProfile> = emptyList(),
    val identifier: String? = null,
    val email: String? = null,
    val sms: String? = null,
    val phone: String? = null,
    val grades: List<String> = emptyList(),
    val password: String? = null,
    val resources: List<OneRosterResourceGUIDRef> = emptyList(),
): OneRosterBase
