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
) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val headerItemCount = 4 //TODO: This MUST be explained
            val fromIndex = from.index - headerItemCount
            val toIndex = to.index - headerItemCount

            if (fromIndex >= 0 && toIndex >= 0 &&
                fromIndex < uiState.sections.size &&
                toIndex < uiState.sections.size) {
                onSectionMoved(fromIndex, toIndex)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
            itemsIndexed(
                items = uiState.sections,
                key = { _, section -> section.uid }
            ) { sectionIndex, section ->
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = section.uid
                ) { isDragging ->
                    SectionItem(
                        section = section,
                        sectionLinkUiState = sectionLinkUiState,
                        sectionIndex = sectionIndex,
                        isDragging = isDragging,
                        onSectionTitleChanged = onSectionTitleChanged,
                        onClickRemoveSection = onClickRemoveSection,
                        onClickAddLesson = onClickAddLesson,
                        onClickRemoveLesson = onClickRemoveLesson,
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
        }
    }
}


@Composable
private fun SectionItem(
    section: CurriculumMappingSection,
    sectionLinkUiState: (CurriculumMappingSectionLink) -> Flow<DataLoadState<CurriculumMappingSectionUiState>>,
    sectionIndex: Int,
    isDragging: Boolean,
    onSectionTitleChanged: (Int, String) -> Unit,
    onClickRemoveSection: (Int) -> Unit,
    onClickAddLesson: (Int) -> Unit,
    onClickRemoveLesson: (Int, Int) -> Unit,
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

            section.items.forEachIndexed { linkIndex, link ->
                LessonItem(
                    link = link,
                    sectionLinkUiState = sectionLinkUiState,
                    sectionIndex = sectionIndex,
                    linkIndex = linkIndex,
                    onClickRemoveLesson = onClickRemoveLesson,
                    enabled = !isDragging
                )
                if (linkIndex < section.items.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
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
    enabled: Boolean
) {

    val stateFlow = remember(link.href) {
        sectionLinkUiState(link)
    }

    val linkUiState by stateFlow.collectAsState(initial = DataLoadingState())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(16.dp))

        IconButton(
            onClick = { onClickRemoveLesson(sectionIndex, linkIndex) },
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
