package world.respect.shared.util

import kotlin.time.Duration

fun Duration.formatToShortString(): String {
    return toComponents { hours, minutes, seconds, _ ->
        buildString {
            if (hours > 0) append("${hours}h:")
            append("${minutes}m:${seconds.toString().padStart(2, '0')}")
        }
    }
}
