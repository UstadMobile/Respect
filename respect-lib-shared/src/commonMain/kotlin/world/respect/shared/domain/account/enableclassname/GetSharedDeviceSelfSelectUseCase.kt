package world.respect.shared.domain.account.enableclassname

import com.russhwolf.settings.Settings

class GetSharedDeviceSelfSelectUseCase(
    private val settings: Settings
) {
    companion object {
        const val PREF_SELF_SELECT_CLASS = "self_select_class"
    }

    operator fun invoke(): Boolean {
        // TODO GET FROM DB
        val isSelfSelectClass = settings.getBoolean(
            key = PREF_SELF_SELECT_CLASS,
            defaultValue = true
        )
        return isSelfSelectClass
    }
}