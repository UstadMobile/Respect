package world.respect.shared.viewmodel.manageuser.profile

import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Keep
@Serializable
enum class ProfileType {
    PARENT,
    CHILD,
    Student
}
