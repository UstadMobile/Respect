package world.respect.datalayer.db.curriculum.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import world.respect.datalayer.db.curriculum.entities.LessonMapping

class MockLessonDataSource {
    private val lessons = MutableStateFlow<List<LessonMapping>>(emptyList())

    fun getLessons(): Flow<List<LessonMapping>> = lessons

    fun getLessonsForChapter(chapterUid: Long): List<LessonMapping> {
        return lessons.value.filter { it.chapterUid == chapterUid }
            .sortedBy { it.lessonNumber }
    }

    fun getLessonsForTextbook(textbookUid: Long): List<LessonMapping> {
        return lessons.value.filter { it.textbookUid == textbookUid }
    }

   fun insertOrUpdate(lesson: LessonMapping) {
        val current = lessons.value.toMutableList()

        val index = current.indexOfFirst { it.uid == lesson.uid }
        if (index >= 0) {
            current[index] = lesson
        } else {
            val newId = if (lesson.uid <= 0L) {
                (current.maxOfOrNull { it.uid } ?: 0L) + 1L
            } else {
                lesson.uid
            }
            current.add(lesson.copy(uid = newId))
        }

        lessons.value = current
    }

   fun delete(lessonUid: Long) {
        val current = lessons.value.toMutableList()
        current.removeAll { it.uid == lessonUid }
        lessons.value = current
    }

    fun deleteAllForChapter(chapterUid: Long) {
        val current = lessons.value.toMutableList()
        current.removeAll { it.chapterUid == chapterUid }
        lessons.value = current
    }

}