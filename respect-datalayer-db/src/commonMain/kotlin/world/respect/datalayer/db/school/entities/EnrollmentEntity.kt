package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * @property eUidNum a snowflake generated uid key, or hash of eUid
 * @property eClassUidNum foreign key as per ClassEntity.cGuidHash . The guid string itself has to
 *           be retrieved from ClassEntity
 *
 * @property ePersonUidNum foreign key as per PersonEntity.pGuidHash . The uid string itself has to
 *           be retrieved from PersonEntity
 * @property eApprovedByPersonUid the person who approved a request to join may, or may not, be
 *           viewable by someone who has permission to see this enrollment. The uid string is
 *           therefor also kept directly on the entity.
 */
@Entity
data class EnrollmentEntity(
    val eUid: String,
    @PrimaryKey
    val eUidNum: Long,
    val eStatus: StatusEnum = StatusEnum.ACTIVE,
    val eLastModified: Instant = Clock.System.now(),
    val eStored: Instant = Clock.System.now(),
    val eMetadata: JsonObject? = null,
    val eClassUidNum: Long,
    val ePersonUidNum: Long,
    val eRole: EnrollmentRoleEnum,
    val eBeginDate: LocalDate? = null,
    val eEndDate: LocalDate? = null,
    val eInviteCode: String? = null,
    val eApprovedByPersonUidNum: Long = 0,
    val eApprovedByPersonUid: String? = null,
)
