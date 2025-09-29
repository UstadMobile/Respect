package world.respect.shared.domain.curriculum.mapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import world.respect.datalayer.db.curriculum.data.MockChapterDataSource
import world.respect.datalayer.db.curriculum.data.MockLessonDataSource
import world.respect.datalayer.db.curriculum.data.MockTextbookDataSource
import world.respect.datalayer.db.curriculum.entities.ChapterMapping
import world.respect.datalayer.db.curriculum.entities.LessonMapping
import world.respect.datalayer.db.curriculum.entities.TextbookMapping

class MockGetCurriculumMappingsUseCaseImpl(
    private val textbookDataSource: MockTextbookDataSource,
    private val chapterDataSource: MockChapterDataSource,
    private val lessonDataSource: MockLessonDataSource,
) : GetCurriculumMappingsUseCase {


    override suspend fun getTextbooks(): Flow<List<TextbookMapping>> {
        return textbookDataSource.getTextbooks()
    }

    override suspend fun getTextbookWithDetails(textbookUid: Long): TextbookMappingWithDetails? {
        val textbooks = textbookDataSource.getTextbooks().first()
        val textbook= textbooks.find { it.uid == textbookUid } ?: return null

        val chapters = chapterDataSource.getChaptersForTextbook(textbookUid)
        val lessons = lessonDataSource.getLessonsForTextbook(textbookUid)

        return TextbookMappingWithDetails(
            textbook = textbook,
            chapters = chapters,
            lessons = lessons
        )
    }

    override suspend fun getChaptersForTextbook(textbookUid: Long): List<ChapterMapping> {
        return chapterDataSource.getChaptersForTextbook(textbookUid)
    }

    override suspend fun getLessonsForChapter(chapterUid: Long): List<LessonMapping> {
        return lessonDataSource.getLessonsForChapter(chapterUid)
    }

    override suspend fun saveTextbook(textbook: TextbookMapping): Long {
        textbookDataSource.insertOrUpdate(textbook)
        return if (textbook.uid == 0L) {
            textbookDataSource.getTextbooks().first()
                .maxByOrNull { it.uid }?.uid ?: 1L
        } else {
            textbook.uid
        }
    }

    override suspend fun saveChapter(chapter: ChapterMapping): Long {
        chapterDataSource.insertOrUpdate(chapter)
        return if (chapter.uid <= 0L) {
            chapterDataSource.getChapters().first()
                .filter { it.textbookUid == chapter.textbookUid }
                .maxByOrNull { it.uid }?.uid ?: 1L
        } else {
            chapter.uid
        }
    }

    override suspend fun saveLesson(lesson: LessonMapping): Long {
        lessonDataSource.insertOrUpdate(lesson)
        return if (lesson.uid <= 0L) {
            lessonDataSource.getLessons().first()
                .filter { it.textbookUid == lesson.textbookUid }
                .maxByOrNull { it.uid }?.uid ?: 1L
        } else {
            lesson.uid
        }
    }

    override suspend fun deleteChapter(chapterUid: Long) {
        lessonDataSource.deleteAllForChapter(chapterUid)
    }

    override suspend fun deleteLesson(lessonUid: Long) {
        lessonDataSource.delete(lessonUid)
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
