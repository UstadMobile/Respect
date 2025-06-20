package world.respect.app.app

import kotlinx.serialization.Serializable
import world.respect.datasource.compatibleapps.model.RespectAppManifest

@Serializable
object AppLauncher
@Serializable
object Assignment
@Serializable
object Clazz
@Serializable
object Report
@Serializable
object AppList
@Serializable
object EnterLink
@Serializable
data class AppsDetail(val manifestUrl: String)
@Serializable
object LessonList
@Serializable
object LessonDetail

