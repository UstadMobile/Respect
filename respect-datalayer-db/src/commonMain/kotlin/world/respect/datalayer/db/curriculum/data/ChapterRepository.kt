package world.respect.datalayer.db.curriculum.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import world.respect.datalayer.db.curriculum.entities.ChapterMapping

object ChapterRepository {
    private val chapters = MutableStateFlow<List<ChapterMapping>>(emptyList())

    fun getChapters(): Flow<List<ChapterMapping>> = chapters

    fun getChaptersForTextbook(textbookUid: Long): List<ChapterMapping> {
        return chapters.value.filter { it.textbookUid == textbookUid }
            .sortedBy { it.chapterNumber }
    }

    suspend fun insertOrUpdate(chapter: ChapterMapping) {
        val current = chapters.value.toMutableList()

        val index = current.indexOfFirst { it.uid == chapter.uid }
        if (index >= 0) {
            current[index] = chapter
        } else {
            val newId = if (chapter.uid <= 0L) {
                (current.maxOfOrNull { it.uid } ?: 0L) + 1L
            } else {
                chapter.uid
            }
            current.add(chapter.copy(uid = newId))
        }

        chapters.value = current
    }

}