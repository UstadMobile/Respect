package world.respect.shared.domain.biometric

import kotlinx.coroutines.channels.Channel

class BiometricAuthUseCaseAndroidChannelHost {
    val requestChannel = Channel<BiometricAuthJob>(capacity = Channel.UNLIMITED)
}
