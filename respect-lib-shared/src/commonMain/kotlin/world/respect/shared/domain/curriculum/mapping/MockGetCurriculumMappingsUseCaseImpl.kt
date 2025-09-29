package world.respect.shared.domain.curriculum.mapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import world.respect.datalayer.db.curriculum.data.ChapterRepository
import world.respect.datalayer.db.curriculum.data.LessonRepository
import world.respect.datalayer.db.curriculum.data.TextbookRepository
import world.respect.datalayer.db.curriculum.entities.ChapterMapping
import world.respect.datalayer.db.curriculum.entities.LessonMapping
import world.respect.datalayer.db.curriculum.entities.TextbookMapping

class MockGetCurriculumMappingsUseCaseImpl(
    private val textbookRepository: TextbookRepository,
    private val chapterRepository: ChapterRepository,
    private val lessonRepository: LessonRepository,
) : GetCurriculumMappingsUseCase {


    override suspend fun getTextbooks(): Flow<List<TextbookMapping>> {
        return textbookRepository.getTextbooks()
    }

    override suspend fun getTextbookWithDetails(textbookUid: Long): TextbookMappingWithDetails? {
        val textbooks = textbookRepository.getTextbooks().first()
        val textbook= textbooks.find { it.uid == textbookUid } ?: return null

        val chapters = chapterRepository.getChaptersForTextbook(textbookUid)
        val lessons = lessonRepository.getLessonsForTextbook(textbookUid)

        return TextbookMappingWithDetails(
            textbook = textbook,
            chapters = chapters,
            lessons = lessons
        )
    }

    override suspend fun getChaptersForTextbook(textbookUid: Long): List<ChapterMapping> {
        return chapterRepository.getChaptersForTextbook(textbookUid)
    }

    override suspend fun getLessonsForChapter(chapterUid: Long): List<LessonMapping> {
        return lessonRepository.getLessonsForChapter(chapterUid)
    }

    override suspend fun saveTextbook(textbook: TextbookMapping): Long {
        textbookRepository.insertOrUpdate(textbook)
        return if (textbook.uid == 0L) {
            textbookRepository.getTextbooks().first()
                .maxByOrNull { it.uid }?.uid ?: 1L
        } else {
            textbook.uid
        }
    }

    override suspend fun saveChapter(chapter: ChapterMapping): Long {
        chapterRepository.insertOrUpdate(chapter)
        return if (chapter.uid <= 0L) {
            chapterRepository.getChapters().first()
                .filter { it.textbookUid == chapter.textbookUid }
                .maxByOrNull { it.uid }?.uid ?: 1L
        } else {
            chapter.uid
        }
    }

    override suspend fun saveLesson(lesson: LessonMapping): Long {
        lessonRepository.insertOrUpdate(lesson)
        return if (lesson.uid <= 0L) {
            lessonRepository.getLessons().first()
                .filter { it.textbookUid == lesson.textbookUid }
                .maxByOrNull { it.uid }?.uid ?: 1L
        } else {
            lesson.uid
        }
    }

    override suspend fun deleteChapter(chapterUid: Long) {
        lessonRepository.deleteAllForChapter(chapterUid)
    }

    override suspend fun deleteLesson(lessonUid: Long) {
        lessonRepository.delete(lessonUid)
    }

    override suspend fun saveCurriculumMapping(
        textbook: TextbookMapping,
        chapters: List<ChapterMapping>,
        lessons: List<LessonMapping>
    ): Long {
        val savedTextbookUid = saveTextbook(textbook)
        val updatedChapters = chapters.map { chapter ->
            val updatedChapter = chapter.copy(textbookUid = savedTextbookUid)
            val savedChapterUid = saveChapter(updatedChapter)
            updatedChapter.copy(uid = savedChapterUid)
        }

        val chapterUidMapping = chapters.zip(updatedChapters)
            .associate { (old, new) -> old.uid to new.uid }

        lessons.forEach { lesson ->
            val newChapterUid = chapterUidMapping[lesson.chapterUid] ?: lesson.chapterUid
            val updatedLesson = lesson.copy(
                textbookUid = savedTextbookUid,
                chapterUid = newChapterUid
            )
            saveLesson(updatedLesson)
        }

        return savedTextbookUid
    }

}
