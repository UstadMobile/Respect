package world.respect.shared.domain.account.enableclassname

import com.russhwolf.settings.Settings

class GetSharedDeviceSelfSelectUseCase(
    private val settings: Settings
) {
    operator fun invoke(): Boolean {
        // TODO GET FROM DB
        val isSelfSelectClass = settings.getBoolean(
            key = "self_select_class",
            defaultValue = true
        )
        return isSelfSelectClass
    }
}