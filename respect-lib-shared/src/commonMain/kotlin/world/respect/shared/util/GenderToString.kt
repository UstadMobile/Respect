package world.respect.shared.util

import org.jetbrains.compose.resources.StringResource

import world.respect.datalayer.oneroster.model.OneRosterGenderEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*

val OneRosterGenderEnum.toGenderLabel: StringResource
    get() = when (this) {
        OneRosterGenderEnum.FEMALE -> Res.string.female
        OneRosterGenderEnum.MALE ->Res.string.male
        OneRosterGenderEnum.OTHER -> Res.string.other
        OneRosterGenderEnum.UNSPECIFIED -> Res.string.unspecified
    }
