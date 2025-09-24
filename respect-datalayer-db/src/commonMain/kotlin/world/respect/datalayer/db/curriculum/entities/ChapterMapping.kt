package world.respect.datalayer.db.curriculum.entities

data class ChapterMapping(
    var uid: Long = 0L,
    var textbookUid: Long = 0L,
    var title: String? = null,
    var chapterNumber: Int = 0,
    var description: String? = null,
    var createdDate: Long = System.currentTimeMillis()
)