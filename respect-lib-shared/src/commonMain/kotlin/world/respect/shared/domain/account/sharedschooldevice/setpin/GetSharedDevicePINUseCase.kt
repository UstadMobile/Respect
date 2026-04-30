package world.respect.shared.domain.account.sharedschooldevice.setpin

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import kotlin.random.Random

interface GetSharedDevicePINUseCase {
    suspend operator fun invoke(): String
}

class GetSharedDevicePINUseCaseImpl(
    private val schoolDataSource: SchoolDataSource,
    private val setSharedDevicePINUseCase: SetSharedDevicePINUseCase
) : GetSharedDevicePINUseCase {

    override suspend fun invoke(): String {
        val existingPin = schoolDataSource.schoolConfigSettingDataSource.findByGuid(
            DataLoadParams(),
            SchoolConfigSettingDataSource.KEY_SHARED_DEVICE_PIN
        ).dataOrNull()

        return if (existingPin != null) {
            existingPin.value
        } else {
            val newPin = generateRandomPin()
            setSharedDevicePINUseCase(newPin)
            newPin
        }
    }

    private fun generateRandomPin(): String {
        return Random.nextInt(1000, 10000).toString().padStart(4, '0')
    }
}
