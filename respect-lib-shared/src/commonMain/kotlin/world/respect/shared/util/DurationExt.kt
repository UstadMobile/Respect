package world.respect.shared.util

import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.duration_hours_short
import world.respect.shared.generated.resources.duration_minutes_short
import kotlin.time.Duration

fun Duration.formatToShortString(): String {
    return toComponents { hours, minutes, seconds, _ ->
        buildString {
            if (hours > 0) append("$hours ${Res.string.duration_hours_short}:")
            append("${minutes}${Res.string.duration_minutes_short}:${seconds.toString().padStart(2, '0')}")
        }
    }
}
