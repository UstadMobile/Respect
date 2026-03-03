package world.respect.shared.domain.account.enableclassname

import com.russhwolf.settings.Settings

class SetSharedDeviceSelfSelectUseCase(
    private val settings: Settings

) {
    companion object {
        const val PREF_SELF_SELECT_CLASS = "self_select_class"
    }

    operator fun invoke(enabled: Boolean) {
        // TODO SAVE TO DB
        settings.putBoolean(PREF_SELF_SELECT_CLASS, enabled)
    }
}