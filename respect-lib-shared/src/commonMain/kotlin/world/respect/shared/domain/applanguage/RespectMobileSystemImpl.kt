package world.respect.shared.domain.applanguage

import org.jetbrains.compose.resources.StringResource

expect class RespectMobileSystemImpl : RespectMobileSystemCommon {

    override fun getString(
        stringResource: StringResource
    ): String

    override fun formatString(
        stringResource: StringResource,
        vararg args: Any
    ): String

    override fun setSystemLocale(
        langCode: String
    )
}
