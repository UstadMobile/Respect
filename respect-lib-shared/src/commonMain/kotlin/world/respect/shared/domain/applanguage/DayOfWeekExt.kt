package world.respect.shared.domain.applanguage

import org.jetbrains.compose.resources.StringResource
import kotlinx.datetime.DayOfWeek
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.friday
import world.respect.shared.generated.resources.monday
import world.respect.shared.generated.resources.saturday
import world.respect.shared.generated.resources.sunday
import world.respect.shared.generated.resources.thursday
import world.respect.shared.generated.resources.tuesday
import world.respect.shared.generated.resources.wednesday


val DayOfWeek.dayStringResource: StringResource
    get() = when(this) {
        DayOfWeek.MONDAY -> Res.string.monday
        DayOfWeek.TUESDAY -> Res.string.tuesday
        DayOfWeek.WEDNESDAY -> Res.string.wednesday
        DayOfWeek.THURSDAY -> Res.string.thursday
        DayOfWeek.FRIDAY -> Res.string.friday
        DayOfWeek.SATURDAY -> Res.string.saturday
        DayOfWeek.SUNDAY -> Res.string.sunday
        else -> Res.string.monday
    }
