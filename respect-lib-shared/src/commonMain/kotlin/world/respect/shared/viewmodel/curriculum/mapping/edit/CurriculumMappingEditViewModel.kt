package world.respect.shared.viewmodel.curriculum.mapping.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.db.curriculum.entities.ChapterMapping
import world.respect.datalayer.db.curriculum.entities.LessonMapping
import world.respect.datalayer.db.curriculum.entities.TextbookMapping
import world.respect.shared.domain.curriculum.mapping.GetCurriculumMappingsUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_unknown
import world.respect.shared.generated.resources.lesson
import world.respect.shared.generated.resources.mapping_edit
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.save_failed
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
    val error: String? = null,
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
    private val getCurriculumMappingsUseCase: GetCurriculumMappingsUseCase,
) : RespectViewModel(savedStateHandle) {

    private val route: CurriculumMappingEdit = savedStateHandle.toRoute()
    private var textbookUid = route.textbookUid

    private val _uiState = MutableStateFlow(
        CurriculumMappingEditUiState(isNew = textbookUid == NEW_TEXTBOOK_UID)
    )
    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)
    private var nextChapterNumber = 1

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

        loadTextbookData()
    }

    private fun loadTextbookData() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            try {
                if (textbookUid != NEW_TEXTBOOK_UID) {
                    val textbookWithDetails = getCurriculumMappingsUseCase.getTextbookWithDetails(textbookUid)
                    if (textbookWithDetails != null) {
                        nextChapterNumber = textbookWithDetails.chapters.size + 1

                        _uiState.update { prev ->
                            prev.copy(
                                textbook = textbookWithDetails.textbook,
                                chapters = textbookWithDetails.chapters,
                                lessons = textbookWithDetails.lessons,
                                loading = false,
                                isNew = false,
                                error = null
                            )
                        }
                    } else {
                        _uiState.update { prev ->
                            prev.copy(
                                loading = false,
                                error = Res.string.error_unknown.asUiText().toString()
                            )
                        }
                    }
                } else {
                    val newTextbook = TextbookMapping().apply {
                        uid = NEW_TEXTBOOK_UID
                        title = ""
                        description = ""
                        coverImageUrl = null
                    }
                    _uiState.update { prev ->
                        prev.copy(
                            textbook = newTextbook,
                            chapters = emptyList(),
                            lessons = emptyList(),
                            loading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Throwable) {
                _uiState.update { prev ->
                    prev.copy(
                        loading = false,
                        error = Res.string.error_unknown.asUiText().toString()
                    )
                }
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
        // TODO:
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

    fun onChapterMoved(fromIndex: Int, toIndex: Int) {
        val currentChapters = _uiState.value.chapters.toMutableList()
        val chapter = currentChapters.removeAt(fromIndex)
        currentChapters.add(toIndex, chapter)

        val updatedChapters = currentChapters.mapIndexed { index, chapterMapping ->
            chapterMapping.copy(chapterNumber = index + 1)
        }

        _uiState.update { prev ->
            prev.copy(chapters = updatedChapters)
        }
    }



    fun onClickAddLesson(chapter: ChapterMapping) {
        val currentLessons = _uiState.value.lessons
        val existingLessonsInChapter = currentLessons.filter { it.chapterUid == chapter.uid }
        val nextLessonNumberForChapter = existingLessonsInChapter.size + 1

        val newLesson = LessonMapping().apply {
            uid = -(System.currentTimeMillis())
            chapterUid = chapter.uid
            lessonNumber = nextLessonNumberForChapter
            title = "Lesson  $nextLessonNumberForChapter"
            subtitle = null
            lessonType = null
            textbookUid = this@CurriculumMappingEditViewModel.textbookUid
        }

        _uiState.update { prev ->
            prev.copy(lessons = currentLessons + newLesson)
        }
    }

    fun onClickRemoveLesson(lesson: LessonMapping) {
        val currentLessons = _uiState.value.lessons
        val updatedLessons = currentLessons.filter { it.uid != lesson.uid }

        val lessonsInChapter = updatedLessons.filter { it.chapterUid == lesson.chapterUid }
        lessonsInChapter.forEachIndexed { index, lessonMapping ->
            lessonMapping.lessonNumber = index + 1
              lessonMapping.title = "Lesson ${index + 1}"

        }

        _uiState.update { prev ->
            prev.copy(lessons = updatedLessons)
        }
    }

    fun onClickSave() {
        val textbook = _uiState.value.textbook ?: return

        val bookTitle = textbook.title
        if (bookTitle.isNullOrBlank()) {
            _uiState.update { prev ->
                prev.copy(bookTitleError = Res.string.required.asUiText().toString())
            }
            return
        } else {
            _uiState.update { prev ->
                prev.copy(bookTitleError = null)
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true) }

                val chapters = _uiState.value.chapters
                val lessons = _uiState.value.lessons

                getCurriculumMappingsUseCase.saveCurriculumMapping(
                    textbook,
                    chapters,
                    lessons
                )

                _uiState.update { it.copy(loading = false) }

                _navCommandFlow.tryEmit(NavCommand.PopUp())

            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = Res.string.save_failed.asUiText().toString()
                    )
                }
            }
        }
    }

    fun onClearError() {
        _uiState.update { prev ->
            prev.copy(
                bookTitleError = null,
                error = null
            )
        }
    }

    fun onRetry() {
        loadTextbookData()
    }

    companion object {
        private const val NEW_TEXTBOOK_UID = 0L
    }
}