package world.respect.shared.domain.account.sharedschooldevice

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.SchoolConfigSetting
import kotlin.time.Clock

class SetSharedDeviceSelfSelectUseCase(
    private val schoolDataSource: SchoolDataSource
) {

    suspend operator fun invoke(enabled: Boolean) {
        val params = DataLoadParams()
        val existingSetting = schoolDataSource.schoolConfigSettingDataSource.findByGuid(
            params, SchoolConfigSettingDataSource.KEY_SHARED_DEVICE_SELF_SELECT
        ).dataOrNull()

        val setting = SchoolConfigSetting(
            key = SchoolConfigSettingDataSource.KEY_SHARED_DEVICE_SELF_SELECT,
            value = enabled.toString(),
            lastModified = Clock.System.now(),
            canRead = existingSetting?.canRead ?: listOf(
                PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                PersonRoleEnum.SITE_ADMINISTRATOR,
                PersonRoleEnum.TEACHER,
                PersonRoleEnum.STUDENT,
                PersonRoleEnum.SHARED_SCHOOL_DEVICE,
                PersonRoleEnum.PARENT
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
