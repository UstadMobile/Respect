package world.respect.shared.domain.account.enableclassname

import com.russhwolf.settings.Settings

class SetSharedDeviceSelfSelectUseCase(
    private val settings: Settings

) {
    operator fun invoke(enabled: Boolean) {
        // TODO SAVE TO DB
        settings.putBoolean("self_select_class", enabled)
    }
}