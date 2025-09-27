package world.respect.shared.domain.getdeviceinfo

fun GetDeviceInfoUseCase.DeviceInfo.toUserFriendlyString(): String {
    return "${this.manufacturer ?: "Unknown Manufacturer"} ${this.model ?: "Unknown Model"}"
}
