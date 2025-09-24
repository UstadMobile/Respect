package world.respect.datalayer.db.curriculum.entities

import java.rmi.server.UID


data class LessonMapping(
    var uid: Long = 0L,
    var chapterUid: Long = 0L,
    var title: String? = null,
    var subtitle: String? = null,
    var lessonType: String? = null,
    var lessonNumber: Int = 0,
    var description: String? = null,
    var createdDate: Long = System.currentTimeMillis(),
    var textbookUid: Long = 0L
)