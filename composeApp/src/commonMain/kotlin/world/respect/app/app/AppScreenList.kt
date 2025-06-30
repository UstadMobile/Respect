package world.respect.app.app

import kotlinx.serialization.Serializable

sealed interface AppDestination

@Serializable
object AppLauncher: AppDestination

@Serializable
object Assignment: AppDestination
@Serializable
object Clazz: AppDestination
@Serializable
object Report: AppDestination
@Serializable
object AppList: AppDestination
@Serializable
object EnterLink: AppDestination
@Serializable
data class AppsDetail(val manifestUrl: String): AppDestination
@Serializable
data class LessonList(val manifestUrl : String): AppDestination
@Serializable
data class LessonDetail(val manifestUrl : String): AppDestination

