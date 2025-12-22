package world.respect.app.view.curriculum.mapping.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.drag
import world.respect.shared.generated.resources.lesson
import world.respect.shared.generated.resources.no_sections_yet
import world.respect.shared.generated.resources.remove_chapter
import world.respect.shared.generated.resources.remove_lesson
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.section
import world.respect.shared.generated.resources.sections
import world.respect.shared.generated.resources.title
import world.respect.shared.generated.resources.section_name
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditUiState
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditViewModel
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingSectionUiState
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSection
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSectionLink
import androidx.compose.ui.draw.alpha


@Composable
fun CurriculumMappingEditScreenForViewModel(
    viewModel: CurriculumMappingEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CurriculumMappingEditScreen(
        uiState = uiState,
        sectionLinkUiState = viewModel::sectionLinkUiStateFor,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onClickAddSection = viewModel::onClickAddSection,
        onClickRemoveSection = viewModel::onClickRemoveSection,
        onSectionTitleChanged = viewModel::onSectionTitleChanged,
        onSectionMoved = viewModel::onSectionMoved,
        onClickAddLesson = viewModel::onClickAddLesson,
        onClickRemoveLesson = viewModel::onClickRemoveLesson,
        onLessonMovedBetweenSections = viewModel::onLessonMovedBetweenSections,
        onClickLesson = viewModel::onClickLesson,
    )
}

@Composable
fun CurriculumMappingEditScreen(
    uiState: CurriculumMappingEditUiState = CurriculumMappingEditUiState(),
    sectionLinkUiState: (CurriculumMappingSectionLink) -> Flow<DataLoadState<CurriculumMappingSectionUiState>>,
    onTitleChanged: (String) -> Unit = {},
    onDescriptionChanged: (String) -> Unit = {},
    onClickAddSection: () -> Unit = {},
    onClickRemoveSection: (Int) -> Unit = {},
    onSectionTitleChanged: (Int, String) -> Unit = { _, _ -> },
    onSectionMoved: (Int, Int) -> Unit = { _, _ -> },
    onClickAddLesson: (Int) -> Unit = {},
    onClickRemoveLesson: (Int, Int) -> Unit = { _, _ -> },
    onLessonMovedBetweenSections: (Int, Int, Int, Int) -> Unit = { _, _, _, _ -> },
    onClickLesson: (CurriculumMappingSectionLink) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    var draggingSectionIndex by remember { mutableStateOf<Int?>(null) }
    var isDraggingAnySection by remember { mutableStateOf(false) }
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val headerItemCount = 4 //TODO: This MUST be explained
            val fromIndex = from.index - headerItemCount
            val toIndex = to.index - headerItemCount

            if (fromIndex >= 0 && toIndex >= 0) {
                var currentItemCount = 0
                var fromSectionIndex = -1
                var fromLessonIndex = -1
                var toSectionIndex = -1
                var toLessonIndex = -1

                for (sectionIndex in uiState.sections.indices) {
                    val section = uiState.sections[sectionIndex]
                    val sectionHeaderIndex = currentItemCount
                    val lessonStartIndex = currentItemCount + 1
                    val lessonEndIndex = lessonStartIndex + section.items.size

                    if (fromIndex == sectionHeaderIndex) {
                        fromSectionIndex = sectionIndex
                        fromLessonIndex = -1
                    } else if (fromIndex in lessonStartIndex until lessonEndIndex) {
                        fromSectionIndex = sectionIndex
                        fromLessonIndex = fromIndex - lessonStartIndex
                    }

                    if (toIndex == sectionHeaderIndex) {
                        toSectionIndex = sectionIndex
                        toLessonIndex = -1
                    } else if (toIndex in lessonStartIndex until lessonEndIndex) {
                        toSectionIndex = sectionIndex
                        toLessonIndex = toIndex - lessonStartIndex
                    }

                    currentItemCount = lessonEndIndex
                }

                when {
                    fromLessonIndex >= 0 && toLessonIndex >= 0 -> {
                        onLessonMovedBetweenSections(
                            fromSectionIndex,
                            fromLessonIndex,
                            toSectionIndex,
                            toLessonIndex
                        )
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    fromLessonIndex >= 0 && toLessonIndex == -1 && toSectionIndex >= 0 -> {
                        onLessonMovedBetweenSections(
                            fromSectionIndex,
                            fromLessonIndex,
                            toSectionIndex,
                            0
                        )
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    fromLessonIndex == -1 && toLessonIndex == -1 &&
                            fromSectionIndex >= 0 && toSectionIndex >= 0 -> {
                        onSectionMoved(fromSectionIndex, toSectionIndex)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            }
        }
    )


    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
    ) {
        item("title") {
            OutlinedTextField(
                value = uiState.mapping?.title ?: "",
                onValueChange = onTitleChanged,
                label = { Text(stringResource(Res.string.title)+ "*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("name"),
                singleLine = true,
                isError = uiState.titleError != null,
                supportingText = {
                    Text(
                        uiTextStringResource(
                            uiState.titleError ?: Res.string.required.asUiText()
                        )
                    )
                }
            )
        }

        item("description") {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChanged,
                label = { Text(stringResource(Res.string.description)) },
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                singleLine = false,
                minLines = 1,
                maxLines = Int.MAX_VALUE
            )
        }

        item("mapping_title") {
            Text(
                modifier = Modifier.defaultItemPadding(),
                text = stringResource(Res.string.sections),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item("add_section_button") {
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddSection()
                },
                headlineContent = {
                    Text(stringResource(Res.string.section))
                },
                leadingContent = {
                    Icon(Icons.Filled.Add, contentDescription = null)
                },
            )
        }

        if (uiState.sections.isEmpty()) {
            item("empty_sections") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.ContentPaste,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.no_sections_yet),
                        modifier = Modifier.sizeIn(maxWidth = 160.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            uiState.sections.forEachIndexed { sectionIndex, section ->
                item(key = "section_header_${section.uid}") {
                    ReorderableItem(
                        state = reorderableLazyListState,
                        key = "section_header_${section.uid}"
                    ) { isDragging ->
                        LaunchedEffect(isDragging) {
                            if (isDragging) {
                                draggingSectionIndex = sectionIndex
                                isDraggingAnySection = true
                            } else {
                                draggingSectionIndex = null
                                isDraggingAnySection = false
                            }
                        }

                        SectionItem(
                            section = section,
                            sectionIndex = sectionIndex,
                            isDragging = isDragging,
                            onSectionTitleChanged = onSectionTitleChanged,
                            onClickRemoveSection = onClickRemoveSection,
                            onClickAddLesson = onClickAddLesson,
                            dragModifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragStopped = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )
                        )
                    }
                }

                section.items.forEachIndexed { linkIndex, link ->
                    item(key = "lesson_${section.uid}_${link.href}_$linkIndex") {
                        ReorderableItem(
                            state = reorderableLazyListState,
                            key = "lesson_${section.uid}_${link.href}_$linkIndex",
                            enabled = !isDraggingAnySection
                        ) { isDragging ->
                            val isParentSectionDragging = draggingSectionIndex == sectionIndex
                            LessonItem(
                                link = link,
                                sectionLinkUiState = sectionLinkUiState,
                                sectionIndex = sectionIndex,
                                linkIndex = linkIndex,
                                onClickRemoveLesson = onClickRemoveLesson,
                                onClickLesson = onClickLesson,
                                isDragging = isDragging,
                                isParentSectionDragging = isParentSectionDragging,
                                dragModifier = Modifier.draggableHandle(
                                    enabled = !isDraggingAnySection,
                                    onDragStarted = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragStopped = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun SectionItem(
    section: CurriculumMappingSection,
    sectionIndex: Int,
    isDragging: Boolean,
    onSectionTitleChanged: (Int, String) -> Unit,
    onClickRemoveSection: (Int) -> Unit,
    onClickAddLesson: (Int) -> Unit,
    dragModifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth().defaultItemPadding(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        )
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
                        modifier = dragModifier
                            .size(24.dp),
                        tint = if (isDragging) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = section.title,
                        label = {
                            Text(stringResource(Res.string.section_name))
                        },
                        onValueChange = { onSectionTitleChanged(sectionIndex, it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isDragging
                    )
                }

                IconButton(
                    onClick = { onClickRemoveSection(sectionIndex) },
                    modifier = Modifier.size(24.dp),
                    enabled = !isDragging
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
                    .padding(start = 32.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onClickAddLesson(sectionIndex) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDragging
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
        }
    }
}

@Composable
private fun LessonItem(
    link: CurriculumMappingSectionLink,
    sectionLinkUiState: (CurriculumMappingSectionLink) -> Flow<DataLoadState<CurriculumMappingSectionUiState>>,
    sectionIndex: Int,
    linkIndex: Int,
    onClickRemoveLesson: (Int, Int) -> Unit,
    onClickLesson: (CurriculumMappingSectionLink) -> Unit = {},
    isDragging: Boolean,
    isParentSectionDragging: Boolean = false,
    dragModifier: Modifier = Modifier
) {

    val stateFlow = remember(link.href) {
        sectionLinkUiState(link)
    }

    val linkUiState by stateFlow.collectAsState(initial = DataLoadingState())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, bottom = 8.dp)
            .then(
                if (isParentSectionDragging) {
                    Modifier.alpha(0.5f)
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = !isDragging && !isParentSectionDragging,
                onClick = { onClickLesson(link) }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.DragHandle,
                contentDescription = stringResource(Res.string.drag),
                modifier = dragModifier.size(20.dp),
                tint = if (isDragging) MaterialTheme.colorScheme.primary
                else if (isParentSectionDragging) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.width(12.dp))

            linkUiState.dataOrNull()?.icon?.also { iconUrl ->
                RespectAsyncImage(
                    uri = iconUrl.toString(),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = link.title ?: "${stringResource(Res.string.lesson)} ${linkIndex + 1}",
                modifier = Modifier.weight(1f),
                color = if (isParentSectionDragging) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(Modifier.width(16.dp))

            IconButton(
                onClick = { onClickRemoveLesson(sectionIndex, linkIndex) },
                modifier = Modifier.size(24.dp),
                enabled = !isDragging && !isParentSectionDragging
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(Res.string.remove_lesson),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}