package world.respect.shared.domain.account.sharedschooldevice

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.SchoolConfigSettingDataSource

class GetSharedDeviceSelfSelectUseCase(
    private val schoolDataSource: SchoolDataSource
) {

    suspend operator fun invoke(): Boolean {
        val setting = schoolDataSource.schoolConfigSettingDataSource.findByGuid(
            DataLoadParams(),
            SchoolConfigSettingDataSource.KEY_SHARED_DEVICE_SELF_SELECT
        ).dataOrNull()

        return setting?.value?.toBoolean() ?: true
    }
}
