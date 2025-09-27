package world.respect.shared.domain.getdeviceinfo

import android.app.ActivityManager
import android.content.Context
import io.github.aakira.napier.Napier

class GetDeviceInfoUseCaseAndroid(
    context: Context
) : GetDeviceInfoUseCase {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    override fun invoke() : GetDeviceInfoUseCase.DeviceInfo {
        val totalMem = try {
            ActivityManager.MemoryInfo().also {
                activityManager.getMemoryInfo(it)
            }.totalMem
        }catch (e: Throwable) {
            Napier.w("Unable to get total memory", e)
            0
        }

        return GetDeviceInfoUseCase.DeviceInfo(
            platform = GetDeviceInfoUseCase.Platform.ANDROID,
            version = android.os.Build.VERSION.RELEASE,
            androidSdkInt = android.os.Build.VERSION.SDK_INT,
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.BRAND,
            ram = totalMem,
        )
    }
}