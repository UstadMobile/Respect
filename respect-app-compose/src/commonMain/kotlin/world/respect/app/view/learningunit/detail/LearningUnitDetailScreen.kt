package world.respect.app.view.learningunit.detail

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
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingSectionUiState
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSectionLink
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailUiState
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel

@Composable
fun LearningUnitDetailScreen(
    viewModel: LearningUnitDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.mapping != null) {
        PlaylistDetailScreen(
            uiState = uiState,
            onClickLesson = viewModel::onClickLesson,
            onClickEdit = viewModel::onClickEdit,
            onClickAssign = viewModel::onClickAssign,
            onClickShare = viewModel::onClickShare,
            onClickCopy = viewModel::onClickCopy,
            onClickDelete = viewModel::onClickDelete,
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

                            Text(
                                text = stringResource(Res.string.app_name),
                            )
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
                    labelText = when(uiState.pinState.status) {
                        PublicationPinState.Status.IN_PROGRESS -> stringResource(Res.string.cancel)
                        PublicationPinState.Status.READY -> stringResource(Res.string.downloaded)
                        else -> stringResource(Res.string.download)
                    },
                    iconContent = {
                        RespectOfflineItemStatusIcon(
                            state = uiState.pinState,
                        )
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
    onClickLesson: (CurriculumMappingSectionLink) -> Unit,
    onClickEdit: () -> Unit,
    onClickAssign: () -> Unit,
    onClickShare: () -> Unit,
    onClickCopy: () -> Unit,
    onClickDelete: () -> Unit,
) {
    var expandedSections by remember { mutableStateOf(setOf<Long>()) }
    val mapping = uiState.mapping

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
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

                            if (!mapping?.description.isNullOrEmpty()) {
                                Text(
                                    text = mapping?.description.orEmpty(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ActionButton(
                                icon = Icons.Default.Share,
                                label = stringResource(Res.string.share),
                                onClick = onClickShare
                            )
                            ActionButton(
                                icon = Icons.Default.ContentCopy,
                                label = stringResource(Res.string.copy),
                                onClick = onClickCopy
                            )
                            ActionButton(
                                icon = Icons.Default.Task,
                                label = stringResource(Res.string.assign),
                                onClick = onClickAssign,
                            )
                            ActionButton(
                                icon = Icons.Default.Delete,
                                label = stringResource(Res.string.delete),
                                onClick = onClickDelete
                            )
                        }
                    }
                }
            }

            mapping?.sections?.forEachIndexed { sectionIndex, section ->
                item(key = "section_${section.uid}") {
                    val isExpanded = expandedSections.contains(section.uid)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultItemPadding()
                            .clickable {
                                expandedSections = if (isExpanded) {
                                    expandedSections - section.uid
                                } else {
                                    expandedSections + section.uid
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = section.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = onClickAssign,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Task,
                                    contentDescription = stringResource(Res.string.assign),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    expandedSections = if (isExpanded) {
                                        expandedSections - section.uid
                                    } else {
                                        expandedSections + section.uid
                                    }
                                },
                                modifier = Modifier
                                    .testTag("expand_collapse_icon_")
                            ) {
                                Icon(
                                    if (isExpanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
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
                            onClickLesson = onClickLesson
                        )
                    }
                }
            }
        }

        if (uiState.showEditButton) {
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
    link: CurriculumMappingSectionLink,
    sectionLinkUiState: (CurriculumMappingSectionLink) -> Flow<DataLoadState<CurriculumMappingSectionUiState>>,
    onClickLesson: (CurriculumMappingSectionLink) -> Unit
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
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
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