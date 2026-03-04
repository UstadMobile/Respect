package world.respect.shared.domain.account.sharedschooldevice.setpin

import org.koin.core.component.KoinComponent

interface SetSharedDevicePINUseCase {
    suspend operator fun invoke(pin: String)
}

class SetSharedDevicePINUseCaseImpl : SetSharedDevicePINUseCase, KoinComponent {

    override suspend fun invoke(pin: String) {
        // Save PIN to database (update if exists, insert if not)
    }
}