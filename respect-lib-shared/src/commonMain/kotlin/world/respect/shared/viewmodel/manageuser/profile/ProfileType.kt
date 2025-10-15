package world.respect.shared.viewmodel.manageuser.profile

import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Keep
@Serializable
enum class ProfileType {

    PARENT,

    //Child profile comes AFTER creation of a Parent profile (student not registering directly)
    CHILD,

    //Student profile is used when a student is registering directly (not via the parent)
    STUDENT,

    TEACHER
}
