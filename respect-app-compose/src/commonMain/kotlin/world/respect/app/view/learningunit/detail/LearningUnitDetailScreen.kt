package world.respect.app.view.learningunit.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libuicompose.theme.black
import com.ustadmobile.libuicompose.theme.white
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.RespectOfflineItemStatusIcon
import world.respect.app.components.RespectQuickActionButton
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.*
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.playlists.mapping.edit.PlaylistSectionUiState
import world.respect.shared.viewmodel.playlists.mapping.model.PlaylistsSectionLink
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailUiState
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel

@Composable
fun LearningUnitDetailScreen(
    viewModel: LearningUnitDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showCopyDialog) {
        CopyPlaylistDialog(
            currentName = uiState.mapping?.title.orEmpty(),
            copyDialogName = uiState.copyDialogName,
            onNameChanged = viewModel::onCopyDialogNameChanged,
            onDismiss = viewModel::onCopyDialogDismiss,
            onConfirm = viewModel::onCopyDialogConfirm
        )
    }

    if (uiState.mapping != null) {
        PlaylistDetailScreen(
            uiState = uiState,
            onClickLesson = viewModel::onClickLesson,
            onClickEdit = viewModel::onClickEdit,
            onClickAssign = viewModel::onClickAssign,
            onClickAssignSection = viewModel::onClickAssignSection,
            onClickShare = viewModel::onClickShare,
            onClickCopy = viewModel::onClickCopy,
            onClickDelete = viewModel::onClickDelete,
            onConfirmSelection = viewModel::onConfirmSelection,
            onClickSelectAll = viewModel::onClickSelectAll,
            onClickSelectNone = viewModel::onClickSelectNone,
            onClickToggleSectionSelection = viewModel::onClickToggleSectionSelection
        )
    } else {
        SingleLessonDetailScreen(
            uiState = uiState,
            onClickOpen = viewModel::onClickOpen,
            onClickDownload = viewModel::onClickDownload,
            onClickAssign = viewModel::onClickAssign,
        )
    }
}

@Composable
private fun CopyPlaylistDialog(
    currentName: String,
    copyDialogName: String,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.make_a_copy),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = copyDialogName,
                    onValueChange = onNameChanged,
                    label = { Text(stringResource(Res.string.name)) },
                    placeholder = { Text("Copy of $currentName") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(Res.string.cancel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(onClick = onConfirm) {
                        Text(
                            text = stringResource(Res.string.copy_playlist),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleLessonDetailScreen(
    uiState: LearningUnitDetailUiState,
    onClickOpen: () -> Unit,
    onClickDownload: () -> Unit,
    onClickAssign: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ListItem(
                leadingContent = {
                    val iconUrl = uiState.lessonDetail?.images?.firstOrNull()?.href

                    RespectAsyncImage(
                        uri = iconUrl,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(120.dp)
                    )
                },
                headlineContent = {
                    Text(
                        text = uiState.lessonDetail?.metadata?.title?.getTitle().orEmpty(),
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingContent = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(white)
                                    .border(1.dp, black, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    modifier = Modifier.padding(6.dp),
                                    contentDescription = null
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(text = stringResource(Res.string.app_name))
                        }

                        Text(
                            text = uiState.lessonDetail?.metadata?.subtitle
                                ?.getTitle().orEmpty()
                        )
                    }
                }
            )
        }

        item {
            Button(
                onClick = onClickOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.open))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RespectQuickActionButton(
                    labelText = when (uiState.pinState.status) {
                        PublicationPinState.Status.IN_PROGRESS -> stringResource(Res.string.cancel)
                        PublicationPinState.Status.READY -> stringResource(Res.string.downloaded)
                        else -> stringResource(Res.string.download)
                    },
                    iconContent = {
                        RespectOfflineItemStatusIcon(state = uiState.pinState)
                    },
                    onClick = onClickDownload,
                    enabled = uiState.buttonsEnabled,
                )

                RespectQuickActionButton(
                    imageVector = Icons.Filled.NearMe,
                    labelText = stringResource(Res.string.assign),
                    onClick = onClickAssign,
                    enabled = uiState.buttonsEnabled,
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailScreen(
    uiState: LearningUnitDetailUiState,
    onClickLesson: (PlaylistsSectionLink) -> Unit,
    onClickEdit: () -> Unit,
    onClickAssign: () -> Unit,
    onClickAssignSection: (Long) -> Unit,
    onClickShare: () -> Unit,
    onClickCopy: () -> Unit,
    onClickDelete: () -> Unit,
    onConfirmSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickSelectNone: () -> Unit,
    onClickToggleSectionSelection: (Long) -> Unit,
) {
    var expandedSections by remember { mutableStateOf(setOf<Long>()) }
    val mapping = uiState.mapping

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = if (uiState.isSelectionMode && uiState.selectedLessons.isNotEmpty()) 88.dp else 16.dp
            )
        ) {
            item("header") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                if (!mapping?.description.isNullOrEmpty()) {
                                    Text(
                                        text = mapping.description,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp,
                                    )
                                }

                                if (mapping?.subject != null || mapping?.grade != null || mapping?.language != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        mapping.subject?.let { subject ->
                                            Text(
                                                text = subject.name.getTitle(),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        mapping.grade?.let { grade ->
                                            Text(
                                                text = grade,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        mapping.language?.let { language ->
                                            Text(
                                                text = language,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (uiState.isSelectionMode && uiState.selectedLessons.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${uiState.selectedLessons.size} lessons selected",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (!uiState.isSelectionMode) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ActionButton(
                                    icon = Icons.Default.Share,
                                    label = stringResource(Res.string.share),
                                    onClick = onClickShare,
                                    modifier = Modifier.testTag("share_btn")
                                )
                                ActionButton(
                                    icon = Icons.Default.ContentCopy,
                                    label = stringResource(Res.string.copy_list),
                                    onClick = onClickCopy,
                                    modifier = Modifier.testTag("copy_btn")
                                )
                                ActionButton(
                                    icon = Icons.Default.Task,
                                    label = stringResource(Res.string.assign),
                                    onClick = onClickAssign,
                                    modifier = Modifier.testTag("assign_btn")
                                )
                                ActionButton(
                                    icon = Icons.Default.Delete,
                                    label = stringResource(Res.string.delete),
                                    onClick = onClickDelete,
                                    modifier = Modifier.testTag("delete_btn")
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isSelectionMode) {
                item("selection_controls") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultItemPadding(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClickSelectAll,
                            modifier = Modifier.testTag("select_all_btn"),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = stringResource(Res.string.select_all),
                                fontSize = 14.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onClickSelectNone,
                            modifier = Modifier.testTag("select_none_btn"),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = stringResource(Res.string.select_none),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            mapping?.sections?.forEachIndexed { sectionIndex, section ->
                item(key = "section_${section.uid}") {
                    val isExpanded = expandedSections.contains(section.uid)
                    val sectionLessons = section.items
                    val allSectionLessonsSelected = sectionLessons.all {
                        uiState.selectedLessons.contains(it)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedSections = if (isExpanded) {
                                    expandedSections - section.uid
                                } else {
                                    expandedSections + section.uid
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isSelectionMode) {
                            Checkbox(
                                checked = allSectionLessonsSelected,
                                onCheckedChange = { onClickToggleSectionSelection(section.uid) },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Text(
                            text = section.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        if (!uiState.isSelectionMode) {
                            IconButton(
                                onClick = { onClickAssignSection(section.uid) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Task,
                                    contentDescription = stringResource(Res.string.assign),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                expandedSections = if (isExpanded) {
                                    expandedSections - section.uid
                                } else {
                                    expandedSections + section.uid
                                }
                            },
                            modifier = Modifier.testTag("expand_collapse_icon_")
                        ) {
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    }
                }

                if (expandedSections.contains(section.uid)) {
                    itemsIndexed(
                        items = section.items,
                        key = { linkIndex, _ -> "lesson_${section.uid}_${linkIndex}" }
                    ) { _, link ->
                        LessonListItem(
                            link = link,
                            sectionLinkUiState = uiState.sectionLinkUiState,
                            onClickLesson = onClickLesson,
                            isSelectionMode = uiState.isSelectionMode,
                            isSelected = uiState.selectedLessons.contains(link)
                        )
                    }
                }
            }
        }

        if (uiState.isSelectionMode && uiState.selectedLessons.isNotEmpty()) {
            Button(
                onClick = onConfirmSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(
                        Res.string.add_tasks_to_assignment,
                        uiState.selectedLessons.size
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else if (uiState.showEditButton) {
            FloatingActionButton(
                onClick = onClickEdit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(Res.string.edit),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(Res.string.edit),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonListItem(
    link: PlaylistsSectionLink,
    sectionLinkUiState: (PlaylistsSectionLink) -> Flow<DataLoadState<PlaylistSectionUiState>>,
    onClickLesson: (PlaylistsSectionLink) -> Unit,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false
) {
    val stateFlow = remember(link.href) {
        sectionLinkUiState(link)
    }
    val linkUiState by stateFlow.collectAsState(initial = DataLoadingState())
    val linkData = linkUiState.dataOrNull()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickLesson(link) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isSelectionMode) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckBox
                else Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        } else {
            linkData?.icon?.let { iconUrl ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    RespectAsyncImage(
                        uri = iconUrl.toString(),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = linkData?.title ?: link.title.orEmpty(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            if (linkData?.subtitle?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = linkData.subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(40.dp),
            enabled = enabled
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 12.sp
        )
    }
}