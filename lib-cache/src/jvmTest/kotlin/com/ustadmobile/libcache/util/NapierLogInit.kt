package com.ustadmobile.libcache.util

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier


private var napierInitDone = false

/**
 * Initialize Napier logging if not already done. Calling this repeatedly within the same JVM run
 * causes log entries to be repeated. Napier.takeLogarithm does not seem to work.
 */
fun initNapierLog() {
    if(!napierInitDone) {
        Napier.base(DebugAntilog())
        napierInitDone = true
    }
}

