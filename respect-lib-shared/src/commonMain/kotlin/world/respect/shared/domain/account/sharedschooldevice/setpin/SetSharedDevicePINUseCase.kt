package world.respect.shared.domain.account.sharedschooldevice.setpin

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.SchoolConfigSetting
import kotlin.time.Clock

interface SetSharedDevicePINUseCase {
    suspend operator fun invoke(pin: String)
}

class SetSharedDevicePINUseCaseImpl(
    private val schoolDataSource: SchoolDataSource
) : SetSharedDevicePINUseCase {

    override suspend fun invoke(pin: String) {
        val params = DataLoadParams()
        val existingSetting = schoolDataSource.schoolConfigSettingDataSource.findByGuid(
            params, SchoolConfigSettingDataSource.KEY_SHARED_DEVICE_PIN
        ).dataOrNull()

        val setting = SchoolConfigSetting(
            key = SchoolConfigSettingDataSource.KEY_SHARED_DEVICE_PIN,
            value = pin,
            lastModified = Clock.System.now(),
            canRead = existingSetting?.canRead ?: listOf(
                PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                PersonRoleEnum.SITE_ADMINISTRATOR,
                PersonRoleEnum.TEACHER
            ),
            canWrite = existingSetting?.canWrite ?: listOf(
                PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                PersonRoleEnum.SITE_ADMINISTRATOR,
                PersonRoleEnum.TEACHER
            )
        )

        schoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))
    }
}
