package world.respect.shared.domain.curriculum.mapping

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.curriculum.entities.ChapterMapping
import world.respect.datalayer.db.curriculum.entities.LessonMapping
import world.respect.datalayer.db.curriculum.entities.TextbookMapping

interface GetCurriculumMappingsUseCase {

    suspend fun getTextbooks(): Flow<List<TextbookMapping>>

    suspend fun getTextbookWithDetails(textbookUid: Long): TextbookMappingWithDetails?

    suspend fun getChaptersForTextbook(textbookUid: Long): List<ChapterMapping>

    suspend fun getLessonsForChapter(chapterUid: Long): List<LessonMapping>
    suspend fun saveTextbook(textbook: TextbookMapping): Long
    suspend fun saveChapter(chapter: ChapterMapping): Long
    suspend fun saveLesson(lesson: LessonMapping): Long
    suspend fun saveCurriculumMapping(
        textbook: TextbookMapping,
        chapters: List<ChapterMapping>,
        lessons: List<LessonMapping>
    ): Long
    suspend fun deleteChapter(chapterUid: Long)
    suspend fun deleteLesson(lessonUid: Long)
}

data class TextbookMappingWithDetails(
    val textbook: TextbookMapping,
    val chapters: List<ChapterMapping>,
    val lessons: List<LessonMapping>
)