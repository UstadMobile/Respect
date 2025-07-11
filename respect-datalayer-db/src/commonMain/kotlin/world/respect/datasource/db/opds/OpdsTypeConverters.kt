package world.respect.datasource.db.opds

import androidx.room.TypeConverter
import world.respect.datasource.db.opds.entities.OpdsFeedMetadataEntity

class OpdsTypeConverters {

    @TypeConverter
    fun fromOpdsTopParentType(value: OpdsParentType): Int {
        return value.id
    }

    @TypeConverter
    fun toOpdsTopParentType(value: Int): OpdsParentType {
        return OpdsParentType.entries.first { it.id == value }
    }

    @TypeConverter
    fun fromOpdsFeedMetadataPropType(value: OpdsFeedMetadataEntity.PropType): Int {
        return value.id
    }

    @TypeConverter
    fun toOpdsFeedMetadataPropType(value: Int): OpdsFeedMetadataEntity.PropType {
        return OpdsFeedMetadataEntity.PropType.entries.first { it.id == value }
    }


}