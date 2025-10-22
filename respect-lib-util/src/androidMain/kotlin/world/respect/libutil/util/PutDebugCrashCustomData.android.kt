package world.respect.libutil.util

import org.acra.ACRA

actual fun putDebugCrashCustomData(key: String, value: String) {
    ACRA.errorReporter.putCustomData(key, value)
}