package com.ustadmobile.libcache.connectivitymonitor

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.Socket

class ConnectivityMonitorJvm(
    private val checkInetAddr: () -> InetAddress = {
        InetAddress.getByName(DEFAULT_CONNECTIVITY_CHECK_HOST)
    },
    private val checkPort: Int = 80,
    private val interval: Int = 5_000,
) : ConnectivityMonitor{


    private val scope = CoroutineScope(Dispatchers.IO + Job())


    private val _state = MutableStateFlow(ConnectivityState(true))

    override val statusFlow: StateFlow<ConnectivityState> = _state.asStateFlow()

    init {
        scope.launch {
            var currentJob: Job? = null
            while(isActive) {
                currentJob?.cancel()
                currentJob = scope.launch {
                    try {
                        Socket(checkInetAddr(), checkPort).close()
                        if(!_state.value.isConnected) {
                            Napier.i("ConnectivityMonitorJvm: Connectivity available/restored")
                            _state.value = ConnectivityState(true)
                        }
                    }catch(e: Throwable) {
                        //Will also catch cancellation exception caused by timeout
                        if(_state.value.isConnected) {
                            Napier.i("ConnectivityMonitorJvm: Connectivity not available/lost")
                            _state.value = ConnectivityState(false)
                        }
                    }
                }
                delay(interval.toLong())
            }
        }
    }

    fun close() {
        scope.cancel()
    }


    companion object {

        const val DEFAULT_CONNECTIVITY_CHECK_HOST = "google.com"
    }
}