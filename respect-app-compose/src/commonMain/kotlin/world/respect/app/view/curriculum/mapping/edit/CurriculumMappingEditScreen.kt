package world.respect.app.view.curriculum.mapping.edit

import world.respect.datalayer.db.curriculum.entities.ChapterMapping
import world.respect.datalayer.db.curriculum.entities.LessonMapping
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditUiState
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditViewModel

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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Book Cover Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onClickAddBookCover,
                modifier = Modifier.testTag("add_book_cover_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = stringResource(Res.string.add_book_cover),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = stringResource(Res.string.add_book_cover),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Book Title Field
        OutlinedTextField(
            value = uiState.bookTitle,
            onValueChange = onBookTitleChanged,
            label = { Text(stringResource(Res.string.book_title)) },
            placeholder = { Text(stringResource(Res.string.required)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("book_title_field"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Book Description Field
        OutlinedTextField(
            value = uiState.bookDescription,
            onValueChange = onBookDescriptionChanged,
            label = { Text(stringResource(Res.string.book_description)) },
            placeholder = { Text(stringResource(Res.string.book_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("book_description_field"),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mapping Section Header
        Text(
            text = stringResource(Res.string.mapping_edit),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Add Chapter Button
        OutlinedButton(
            onClick = onClickAddChapter,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_chapter_button")
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.chapter))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chapters list or empty
        if (uiState.chapters.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Add,
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
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.chapters) { chapter ->
                    ChapterItem(
                        chapter = chapter,
                        lessons = uiState.getLessonsForChapter(chapter.uid),
                        onClickRemoveChapter = onClickRemoveChapter,
                        onClickAddLesson = onClickAddLesson,
                        onClickRemoveLesson = onClickRemoveLesson
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save or Submit Button
        Button(
            onClick = onClickSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.save))
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: ChapterMapping,
    lessons: List<LessonMapping>,
    onClickRemoveChapter: (ChapterMapping) -> Unit,
    onClickAddLesson: (ChapterMapping) -> Unit,
    onClickRemoveLesson: (LessonMapping) -> Unit
) {
    Column {
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
                modifier = Modifier.size(24.dp)
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
                .padding(start = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onClickAddLesson(chapter) },
                modifier = Modifier.testTag("add_lesson_button_${chapter.uid}")
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
                onClickRemoveLesson = onClickRemoveLesson
            )
        }
    }
}

@Composable
private fun LessonItem(
    lesson: LessonMapping,
    onClickRemoveLesson: (LessonMapping) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp).padding (vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            val indicatorColor = when (lesson.lessonType) {
                "A" -> Color(0xFFFFA726)
                "B" -> Color(0xFFEF5350)
                "C" -> Color(0xFF66BB6A)
                "D" -> Color(0xFF9C27B0)
                else -> MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = indicatorColor,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lesson.lessonType ?: "?",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = lesson.title ?: stringResource(Res.string.simple_learning),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                lesson.subtitle?.let { subtitle ->
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

        IconButton(
            onClick = { onClickRemoveLesson(lesson) },
            modifier = Modifier.size(24.dp)
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
        onClickSave = viewModel::onClickSave
    )
}
