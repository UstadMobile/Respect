package world.respect.datalayer.db.curriculum.entities

data class ChapterMapping(
    val uid: Long = 0L,
    val textbookUid: Long = 0L,
    val title: String? = null,
    val chapterNumber: Int = 0,
    val description: String? = null,
    val createdDate: Long = System.currentTimeMillis()
)