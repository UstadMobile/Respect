

package world.respect.shared.viewmodel.curriculum.mapping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import world.respect.datalayer.db.curriculum.entities.ChapterMapping
import world.respect.datalayer.db.curriculum.entities.LessonMapping
import world.respect.datalayer.db.curriculum.entities.TextbookMapping
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.mapping_edit
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState


data class CurriculumMappingEditUiState(
    val textbook: TextbookMapping? = null,
    val chapters: List<ChapterMapping> = emptyList(),
    val lessons: List<LessonMapping> = emptyList(),
    val bookTitleError: String? = null,
    val loading: Boolean = false,
    val isNew: Boolean = true,
) {

    val fieldsEnabled: Boolean
        get() = !loading

    val bookTitle: String
        get() = textbook?.title ?: ""

    val bookDescription: String
        get() = textbook?.description ?: ""

    fun getLessonsForChapter(chapterUid: Long): List<LessonMapping> {
        return lessons.filter { it.chapterUid == chapterUid }
    }
}

class CurriculumMappingEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
) : RespectViewModel(savedStateHandle) {

    private val route: CurriculumMappingEdit = savedStateHandle.toRoute()
    private var textbookUid = route.textbookUid

    private val _uiState = MutableStateFlow(CurriculumMappingEditUiState(isNew = textbookUid == 0L))
    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)
    private var nextChapterNumber = 1
    private var nextLessonNumber = 1

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.mapping_edit.asUiText(),
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave
                ),
                hideBottomNavigation = true,
            )
        }

        viewModelScope.launch {
            if (textbookUid != 0L) {
                loadTextbookData()
            } else {
                val newTextbook = TextbookMapping().apply {
                    uid = 0L
                    title = ""
                    description = ""
                    coverImageUrl = null
                }
                _uiState.update { prev ->
                    prev.copy(
                        textbook = newTextbook,
                        chapters = emptyList(),
                        lessons = emptyList(),
                        loading = false
                    )
                }
            }
        }
    }

    private fun loadTextbookData() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            val mockTextbook = TextbookMapping().apply {
                uid = textbookUid
                title = "English, Grade 1"
                description = "English curriculum for grade 1 students"
                coverImageUrl = null
            }

            val mockChapters = listOf(
                ChapterMapping().apply {
                    uid = 1L
                    chapterNumber = 1
                    title = null
                    textbookUid = this@CurriculumMappingEditViewModel.textbookUid
                },
                ChapterMapping().apply {
                    uid = 2L
                    chapterNumber = 2
                    title = null
                    textbookUid = this@CurriculumMappingEditViewModel.textbookUid
                }
            )

            val mockLessons = listOf(
                LessonMapping().apply {
                    uid = 1L
                    chapterUid = 1L
                    lessonNumber = 1
                    title = "Alphabet A"
                    subtitle = "Chapter learning"
                    lessonType = "A"
                    textbookUid = this@CurriculumMappingEditViewModel.textbookUid
                },
                LessonMapping().apply {
                    uid = 2L
                    chapterUid = 1L
                    lessonNumber = 2
                    title = "Alphabet B"
                    subtitle = "Chapter learning"
                    lessonType = "B"
                    textbookUid = this@CurriculumMappingEditViewModel.textbookUid
                },
                LessonMapping().apply {
                    uid = 3L
                    chapterUid = 2L
                    lessonNumber = 1
                    title = "Alphabet C"
                    subtitle = "Chapter learning"
                    lessonType = "C"
                    textbookUid = this@CurriculumMappingEditViewModel.textbookUid
                },
                LessonMapping().apply {
                    uid = 4L
                    chapterUid = 2L
                    lessonNumber = 2
                    title = "Alphabet D"
                    subtitle = "Chapter learning"
                    lessonType = "D"
                    textbookUid = this@CurriculumMappingEditViewModel.textbookUid
                }
            )

            nextChapterNumber = mockChapters.size + 1
            nextLessonNumber = mockLessons.size + 1

            _uiState.update { prev ->
                prev.copy(
                    textbook = mockTextbook,
                    chapters = mockChapters,
                    lessons = mockLessons,
                    loading = false,
                    isNew = false
                )
            }
        }
    }

    fun onBookTitleChanged(title: String) {
        val currentTextbook = _uiState.value.textbook ?: return
        val updatedTextbook = currentTextbook.copy(title = title)

        _uiState.update { prev ->
            prev.copy(textbook = updatedTextbook)
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = updatedTextbook.title
        }
    }

    fun onBookDescriptionChanged(description: String) {
        val currentTextbook = _uiState.value.textbook ?: return

        val updatedTextbook = currentTextbook.copy(description = description)

        _uiState.update { prev ->
            prev.copy(textbook = updatedTextbook)
        }

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = updatedTextbook.description
        }
    }


    fun onClickAddBookCover() {
       //TODO
    }

    fun onClickAddChapter() {
        val currentChapters = _uiState.value.chapters

        val newChapter = ChapterMapping().apply {
            uid = -(System.currentTimeMillis())
            chapterNumber = nextChapterNumber++
            title = null
            textbookUid = this@CurriculumMappingEditViewModel.textbookUid
        }

        _uiState.update { prev ->
            prev.copy(chapters = currentChapters + newChapter)
        }
    }

    fun onClickRemoveChapter(chapter: ChapterMapping) {
        val currentChapters = _uiState.value.chapters
        val currentLessons = _uiState.value.lessons

        val updatedChapters = currentChapters.filter { it.uid != chapter.uid }
        val updatedLessons = currentLessons.filter { it.chapterUid != chapter.uid }

        _uiState.update { prev ->
            prev.copy(
                chapters = updatedChapters,
                lessons = updatedLessons
            )
        }
    }

    fun onClickAddLesson(chapter: ChapterMapping) {
        val currentLessons = _uiState.value.lessons
        val lessonTypes = arrayOf("A", "B", "C", "D")

        val newLesson = LessonMapping().apply {
            uid = -(System.currentTimeMillis())
            chapterUid = chapter.uid
            lessonNumber = nextLessonNumber++
            title = null
            subtitle = "Chapter learning"
            lessonType = lessonTypes[(nextLessonNumber - 2) % lessonTypes.size]
            textbookUid = this@CurriculumMappingEditViewModel.textbookUid
        }

        _uiState.update { prev ->
            prev.copy(lessons = currentLessons + newLesson)
        }
    }

    fun onClickRemoveLesson(lesson: LessonMapping) {
        val currentLessons = _uiState.value.lessons
        val updatedLessons = currentLessons.filter { it.uid != lesson.uid }

        _uiState.update { prev ->
            prev.copy(lessons = updatedLessons)
        }
    }

    fun onClickSave() {
        val textbook = _uiState.value.textbook
        if (textbook == null) return

        textbook.title?.let {
            if (it.isBlank()) {
                _uiState.update { prev ->
                    prev.copy(bookTitleError = Res.string.required.asUiText().toString())
                }
                return
            } else {
                _uiState.update { prev -> prev.copy(bookTitleError = null) }
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true) }

                kotlinx.coroutines.delay(SAVE_DELAY_MS)

                _uiState.update { it.copy(loading = false) }
                _navCommandFlow.tryEmit(NavCommand.PopUp())

            } catch (e: Throwable) {
                _uiState.update { it.copy(loading = false) }
                e.printStackTrace()
            }
        }
    }

    fun onClearError() {
        _uiState.update { prev -> prev.copy(bookTitleError = null) }
    }

    companion object {
        private const val SAVE_DELAY_MS = 500L
    }
}