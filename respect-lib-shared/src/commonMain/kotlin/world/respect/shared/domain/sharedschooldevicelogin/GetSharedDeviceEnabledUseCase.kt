package world.respect.shared.domain.sharedschooldevicelogin

import com.russhwolf.settings.Settings

class GetSharedDeviceEnabledUseCase(
    private val settings: Settings
) {
    operator fun invoke(): Boolean {
        return settings.getStringOrNull(SetSharedDeviceEnabledUseCase.KEY_SHARED_DEVICE)
            ?.toBoolean() ?: false
    }
}