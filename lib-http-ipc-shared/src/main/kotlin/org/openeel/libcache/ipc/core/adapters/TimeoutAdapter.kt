package org.openeel.libcache.ipc.core.adapters

import android.os.Bundle
import okio.Timeout
import org.openeel.libcache.ipc.core.HttpIpcKeys
import java.util.concurrent.TimeUnit

fun Timeout.toBundle(): Bundle {
    return Bundle().also {
        it.putLong(HttpIpcKeys.KEY_TIMEOUT_NANOS, timeoutNanos())
    }
}

fun Bundle.toTimeout() : Timeout {
    return Timeout().timeout(
        timeout = getLong(HttpIpcKeys.KEY_TIMEOUT_NANOS),
        unit = TimeUnit.NANOSECONDS
    )
}