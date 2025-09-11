package world.respect.datalayer.db.school

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.StatusEnum
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

}