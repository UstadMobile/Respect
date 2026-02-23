package world.respect.shared.domain.account.setpin

import kotlin.random.Random

interface GetSharedDevicePINUseCase {
    suspend operator fun invoke(): Result<String>
}

class GetSharedDevicePINUseCaseImpl : GetSharedDevicePINUseCase {
    override suspend fun invoke(): Result<String> {
        return try {
            // Check if PIN exists in database for this school/device
            val existingPin = null

            if (existingPin != null) {
                Result.success(existingPin)
            } else {
                val newPin = generateRandomPin()
                Result.success(newPin)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateRandomPin(): String {
        return Random.nextInt(1000, 10000).toString().padStart(4, '0')
    }
}