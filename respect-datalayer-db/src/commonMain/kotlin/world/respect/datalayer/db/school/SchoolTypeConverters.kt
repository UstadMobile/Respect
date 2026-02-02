package world.respect.datalayer.db.school

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.StatusEnum
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.school.model.AssignmentAssigneeRefTypeEnum
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.school.writequeue.WriteQueueItem
import kotlin.time.Instant

class SchoolTypeConverters {

    @TypeConverter
    fun toPersonRoleType(value: Int): PersonRoleEnum {
        return PersonRoleEnum.fromFlag(value)
    }

    @TypeConverter
    fun fromPersonRoleType(value: PersonRoleEnum): Int {
        return value.flag
    }

    /**
     * Convert a LocalDate to/from a Long. This is always done in millis since epoch at start of day
     * UTC.
     */
    @TypeConverter
    fun toLocalDate(value: Long?) : LocalDate? {
        return value?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
        }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?) : Long? {
        return value?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toStatusEnum(value: Int): StatusEnum {
        return StatusEnum.fromFlag(value)
    }

    @TypeConverter
    fun fromStatusEnum(value: StatusEnum): Int {
        return value.flag
    }

    // --- For JsonObject ---
    @TypeConverter
    fun fromJsonObject(value: JsonObject?): String? =
        value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toJsonObject(value: String?): JsonObject? =
        value?.let { Json.decodeFromString<JsonObject>(it) }


    @TypeConverter
    fun fromWriteQueueItemModel(value: WriteQueueItem.Model): Int {
        return value.flag
    }

    @TypeConverter
    fun toWriteQueueItemModel(value: Int): WriteQueueItem.Model {
        return WriteQueueItem.Model.fromFlag(value)
    }

    @TypeConverter
    fun fromPersonStatusEnum(value: PersonStatusEnum): Int {
        return value.flag
    }

    @TypeConverter
    fun toPersonStatusEnum(value: Int): PersonStatusEnum {
        return PersonStatusEnum.fromFlag(value)
    }


    @TypeConverter
    fun fromPersonGenderEnum(value: PersonGenderEnum): Int {
        return value.flag
    }

    @TypeConverter
    fun toPersonGenderEnum(value: Int): PersonGenderEnum {
        return PersonGenderEnum.fromFlag(value)
    }

    @TypeConverter
    fun fromEnrollmentRoleEnum(value: EnrollmentRoleEnum) : Int {
        return value.flag
    }

    @TypeConverter
    fun toEnrollmentRoleEnum(value: Int): EnrollmentRoleEnum {
        return EnrollmentRoleEnum.fromFlag(value)
    }

    @TypeConverter
    fun fromAssignmentAssigneeRefTypeEnum(value: AssignmentAssigneeRefTypeEnum): Int {
        return value.flag
    }

    @TypeConverter
    fun toAssignmentAssigneeRefTypeEnum(value: Int): AssignmentAssigneeRefTypeEnum {
        return AssignmentAssigneeRefTypeEnum.fromFlag(value)
    }

    @TypeConverter
    fun fromClassInviteModeEnum(value: ClassInviteModeEnum): Int {
        return value.flag
    }

    @TypeConverter
    fun toClassInviteModeEnum(value: Int): ClassInviteModeEnum {
        return ClassInviteModeEnum.fromFlag(value)
    }


}