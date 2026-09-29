package com.ustadmobile.libcache.db

import androidx.room.TypeConverter
import com.ustadmobile.libcache.PublicationPinState
import io.ktor.http.Url

class DbTypeConverters {

    @TypeConverter
    fun fromUrl(value: Url?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toUrl(value: String?): Url? {
        return value?.let { Url(it) }
    }

    @TypeConverter
    fun fromPublicationPinStateStatus(value: PublicationPinState.Status): Int {
        return value.flagVal
    }

    @TypeConverter
    fun toPublicationPinStateStatus(value: Int): PublicationPinState.Status {
        return PublicationPinState.Status.entries.first { it.flagVal == value }
    }

}