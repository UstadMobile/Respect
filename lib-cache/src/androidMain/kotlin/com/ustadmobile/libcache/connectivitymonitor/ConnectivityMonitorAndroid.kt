package com.ustadmobile.libcache.connectivitymonitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectivityMonitorAndroid(
    context: Context
) : ConnectivityMonitor {

    private val _status = MutableStateFlow(ConnectivityState(isConnected = false))

    override val statusFlow: StateFlow<ConnectivityState> = _status.asStateFlow()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _status.value = ConnectivityState(isConnected = true)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            Log.i(LOGTAG, "onCapabilitiesChanged: isConnected=$isConnected")

            _status.value = ConnectivityState(
                isConnected = isConnected
            )
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.i(LOGTAG, "onLost: isConnected=false")
            _status.value = ConnectivityState(isConnected = false)
        }
    }

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        Log.i(LOGTAG, "Initialized: callback registered")
    }

    companion object {

        private const val LOGTAG = "ConnectivityMonitorAndroid"
    }

}