package com.ustadmobile.libcache.connectivitymonitor

import kotlinx.coroutines.flow.StateFlow

/**
 * Very basic at the moment - metered status etc to follow later.
 */
data class ConnectivityState(
    val isConnected: Boolean,
)

interface ConnectivityMonitor {

    val statusFlow: StateFlow<ConnectivityState>

}
