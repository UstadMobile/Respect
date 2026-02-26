package world.respect.shared.domain.account.setpin

import org.koin.core.component.KoinComponent
import kotlin.random.Random

interface GetSharedDevicePINUseCase {
    suspend operator fun invoke(): String
}

class GetSharedDevicePINUseCaseImpl : GetSharedDevicePINUseCase, KoinComponent {

    override suspend fun invoke(): String {
        // Check if PIN exists in database for this school/device
        val existingPin = null
        return if (existingPin != null) {
            existingPin
        } else {
            val newPin = generateRandomPin()
            // Save the generated PIN to database
            newPin
        }
    }

    private fun generateRandomPin(): String {
        return Random.nextInt(1000, 10000).toString().padStart(4, '0')
    }
}