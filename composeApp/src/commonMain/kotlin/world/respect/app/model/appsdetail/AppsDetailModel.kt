package world.respect.app.model.appsdetail

data class LessonItem(
    val title: String,
    )

data class AppsDetailModel(
    val imageName: String,
    val appName: String,
    val appDescription: String,
    val lessons: List<LessonItem>
)

