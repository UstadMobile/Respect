package com.ustadmobile.libcache.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param neighborUid the xxhash of neighborUrl
 * @param neighborDeviceName the device name of the neighboring cache as per its ping message
 * @param neighborIp the IP address of the neighbor
 * @param neighborUdpPort the UDP port of the neighbor used to exchange available entries
 * @param neighborHttpPort the HTTP port of the neighbor used to download actual entries
 * @param neighborDiscovered the time the neighbor was discovered
 */
@Entity
data class NeighborCache(
    @PrimaryKey
    val neighborUid: Long = 0L,
    val neighborDeviceName: String = "",
    val neighborIp: String = "",
    val neighborUdpPort: Int = 0,
    val neighborHttpPort: Int = 0,
    val neighborDiscovered: Long = 0L,
    val neighborPingTime: Int = 0,
    val neighborLastSeen: Long = 0L,
    val neighborStatus: Int = 1,
) {

    companion object {

        const val STATUS_ACTIVE = 1

        @Suppress("unused")
        const val STATUS_LOST = 0

    }

}
