package world.respect.shared.domain.sharedschooldevicelogin

import com.russhwolf.settings.Settings

class SetSharedDeviceEnabledUseCase(
    private val settings: Settings,
) {
    operator fun invoke(enabled: Boolean) {
        settings.putString(KEY_SHARED_DEVICE, enabled.toString())
    }

    companion object {
        const val KEY_SHARED_DEVICE = "sharedDevice"
    }
}