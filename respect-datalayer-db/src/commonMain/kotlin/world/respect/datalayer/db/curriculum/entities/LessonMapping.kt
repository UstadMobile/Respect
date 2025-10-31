package world.respect.datalayer.db.curriculum.entities

data class LessonMapping(
    val uid: Long = 0L,
    val chapterUid: Long = 0L,
    val title: String? = null,
    val subtitle: String? = null,
    val lessonType: String? = null,
    val lessonNumber: Int = 0,
    val description: String? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val textbookUid: Long = 0L
)