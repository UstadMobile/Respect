package world.respect.app.view.curriculum.mapping.edit

import world.respect.datalayer.db.curriculum.entities.ChapterMapping
import world.respect.datalayer.db.curriculum.entities.LessonMapping
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_book_cover
import world.respect.shared.generated.resources.book_description
import world.respect.shared.generated.resources.book_title
import world.respect.shared.generated.resources.chapter
import world.respect.shared.generated.resources.click_plus_button
import world.respect.shared.generated.resources.drag
import world.respect.shared.generated.resources.error_unknown
import world.respect.shared.generated.resources.lesson
import world.respect.shared.generated.resources.mapping
import world.respect.shared.generated.resources.no_chapter_added
import world.respect.shared.generated.resources.remove_chapter
import world.respect.shared.generated.resources.remove_lesson
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.to_add_one
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditUiState
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurriculumMappingEditScreen(
    uiState: CurriculumMappingEditUiState = CurriculumMappingEditUiState(),
    onBookTitleChanged: (String) -> Unit = { },
    onBookDescriptionChanged: (String) -> Unit = { },
    onClickAddBookCover: () -> Unit = { },
    onClickAddChapter: () -> Unit = { },
    onClickRemoveChapter: (ChapterMapping) -> Unit = { },
    onClickAddLesson: (ChapterMapping) -> Unit = { },
    onClickRemoveLesson: (LessonMapping) -> Unit = { },
    onClickSave: () -> Unit = { },
    onChapterMoved: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
) {
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onClickAddBookCover,
                        modifier = Modifier.testTag("add_book_cover_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(250.dp)
                                    .background(
                                        color = Color.Gray.copy(alpha = 0.6f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Filled.AddAPhoto,
                                        contentDescription = stringResource(Res.string.add_book_cover),
                                        modifier = Modifier.size(100.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = stringResource(Res.string.add_book_cover),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(2.dp))
                uiState.error?.let {
                    Text(
                        text = stringResource(Res.string.error_unknown),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = uiState.bookTitle,
                    onValueChange = onBookTitleChanged,
                    label = { Text(stringResource(Res.string.book_title)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_title_field"),
                    singleLine = true,
                    isError = uiState.bookTitleError != null ,
                    supportingText = {
                        if (uiState.bookTitleError != null) {
                            Text(stringResource(Res.string.required))
                        } else {
                            Text(stringResource(Res.string.required))
                        }
                    },
                    enabled = uiState.fieldsEnabled
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.bookDescription,
                    onValueChange = onBookDescriptionChanged,
                    label = { Text(stringResource(Res.string.book_description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_description_field"),
                    singleLine = false,
                    minLines = 1,
                    maxLines = Int.MAX_VALUE,
                    enabled = uiState.fieldsEnabled
                )
            }
            item {
                Text(
                    text = stringResource(Res.string.mapping),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedButton(
                    onClick = onClickAddChapter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_chapter_button"),
                    enabled = uiState.fieldsEnabled
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.chapter))
                    }
                }
            }

            if (uiState.chapters.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.no_chapter_added),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(Res.string.click_plus_button),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(Res.string.to_add_one),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = uiState.chapters,
                    key = { _, chapter -> chapter.uid }
                ) { index, chapter ->
                    val isDragging = draggedItemIndex == index

                    DraggableChapterItem(
                        chapter = chapter,
                        index = index,
                        isDragging = isDragging,
                        dragOffset = if (isDragging) dragOffset else 0f,
                        lessons = uiState.getLessonsForChapter(chapter.uid),
                        onClickRemoveChapter = onClickRemoveChapter,
                        onClickAddLesson = onClickAddLesson,
                        onClickRemoveLesson = onClickRemoveLesson,
                        enabled = uiState.fieldsEnabled && !isDragging,
                        onDragStart = {
                            draggedItemIndex = index
                            dragOffset = 0f
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDragEnd = {
                            draggedItemIndex = -1
                            dragOffset = 0f
                        },
                        onDrag = { delta ->
                            dragOffset += delta
                            val itemHeight = 120f
                            val swapThreshold = itemHeight / 2

                            when {
                                dragOffset > swapThreshold && index < uiState.chapters.size - 1 -> {
                                    onChapterMoved(index, index + 1)
                                    draggedItemIndex = index + 1
                                    dragOffset = 0f
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                dragOffset < -swapThreshold && index > 0 -> {
                                    onChapterMoved(index, index - 1)
                                    draggedItemIndex = index - 1
                                    dragOffset = 0f
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableChapterItem(
    chapter: ChapterMapping,
    index: Int,
    isDragging: Boolean,
    dragOffset: Float,
    lessons: List<LessonMapping>,
    onClickRemoveChapter: (ChapterMapping) -> Unit,
    onClickAddLesson: (ChapterMapping) -> Unit,
    onClickRemoveLesson: (LessonMapping) -> Unit,
    enabled: Boolean = true,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDrag: (Float) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = dragOffset
                alpha = if (isDragging) 0.7f else 1f
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
            }
            .zIndex(if (isDragging) 1f else 0f),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.DragHandle,
                        contentDescription = stringResource(Res.string.drag),
                        modifier = Modifier
                            .size(24.dp)
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        onDragStart()
                                    },
                                    onDragEnd = {
                                        onDragEnd()
                                    },
                                    onDrag = { _, dragAmount ->
                                        onDrag(dragAmount.y)
                                    }
                                )
                            },
                        tint = if (isDragging) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = chapter.title
                            ?: "${stringResource(Res.string.chapter)} ${chapter.chapterNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onClickRemoveChapter(chapter) },
                    modifier = Modifier.size(24.dp),
                    enabled = enabled
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.remove_chapter),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onClickAddLesson(chapter) },
                    modifier = Modifier.testTag("add_lesson_button_${chapter.uid}"),
                    enabled = enabled
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.lesson))
                }
            }

            lessons.forEach { lesson ->
                LessonItem(
                    lesson = lesson,
                    onClickRemoveLesson = onClickRemoveLesson,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun LessonItem(
    lesson: LessonMapping,
    onClickLesson: (LessonMapping) -> Unit = {},
    onClickRemoveLesson: (LessonMapping) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClickLesson(lesson) }
            .padding(horizontal = 40.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            val lessonTitle = lesson.title ?: stringResource(Res.string.lesson,lesson.lessonNumber)
            val initial = lessonTitle.firstOrNull()?.uppercaseChar()?.toString() ?: " "
            val indicatorColor = MaterialTheme.colorScheme.primary

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = indicatorColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = lessonTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                lesson.subtitle?.let { subtitle ->
                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = { onClickRemoveLesson(lesson) },
            modifier = Modifier.size(24.dp),
            enabled = enabled
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(Res.string.remove_lesson),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CurriculumMappingEditScreenForViewModel(
    viewModel: CurriculumMappingEditViewModel
) {
    val uiState = viewModel.uiState.collectAsState().value
    CurriculumMappingEditScreen(
        uiState = uiState,
        onBookTitleChanged = viewModel::onBookTitleChanged,
        onBookDescriptionChanged = viewModel::onBookDescriptionChanged,
        onClickAddBookCover = viewModel::onClickAddBookCover,
        onClickAddChapter = viewModel::onClickAddChapter,
        onClickRemoveChapter = viewModel::onClickRemoveChapter,
        onClickAddLesson = viewModel::onClickAddLesson,
        onClickRemoveLesson = viewModel::onClickRemoveLesson,
        onClickSave = viewModel::onClickSave,
        onChapterMoved = viewModel::onChapterMoved
    )
}