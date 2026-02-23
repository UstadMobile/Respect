package world.respect.shared.domain.account.setpin

interface SetSharedDevicePINUseCase {
    suspend operator fun invoke(pin: String): Result<Unit>
}

class SetSharedDevicePINUseCaseImpl : SetSharedDevicePINUseCase {
    override suspend fun invoke(pin: String): Result<Unit> {
        return try {
            // TODO Attempt to save to database
//            schoolDataSource.saveSharedDevicePIN(pin)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}