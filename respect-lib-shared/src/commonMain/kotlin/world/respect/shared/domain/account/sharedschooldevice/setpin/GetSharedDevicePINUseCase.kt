package world.respect.shared.domain.account.sharedschooldevice.setpin

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.SchoolConfigSetting
import kotlin.random.Random

interface GetSharedDevicePINUseCase {
    suspend operator fun invoke(): String
}

class GetSharedDevicePINUseCaseImpl(
    private val schoolDataSource: SchoolDataSource
) : GetSharedDevicePINUseCase {

    override suspend fun invoke(): String {
        val params = DataLoadParams()
        val existingPin = schoolDataSource.schoolConfigSettingDataSource.findByGuid(
            params, SchoolConfigSettingDataSource.KEY_SHARED_DEVICE_PIN
        ).dataOrNull()
        println("Existing PIN: $existingPin")

        return if (existingPin != null) {
            println("Existing PIN: ${existingPin.value}")
            existingPin.value
        } else {
            println("Existing PIN: null")
            val newPin = generateRandomPin()
            println("Existing PIN Generated new PIN: $newPin")
            val setting = SchoolConfigSetting(
                key = SchoolConfigSettingDataSource.KEY_SHARED_DEVICE_PIN,
                value = newPin,
                canRead = listOf(
                    PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                    PersonRoleEnum.SITE_ADMINISTRATOR,
                    PersonRoleEnum.TEACHER
                ),
                canWrite = listOf(
                    PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                    PersonRoleEnum.SITE_ADMINISTRATOR,
                    PersonRoleEnum.TEACHER
                )
            )
            schoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))
            newPin
        }
    }

    private fun generateRandomPin(): String {
        return Random.nextInt(1000, 10000).toString().padStart(4, '0')
    }
}
