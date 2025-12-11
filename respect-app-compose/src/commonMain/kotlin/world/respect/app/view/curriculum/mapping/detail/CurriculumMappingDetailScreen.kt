package world.respect.app.view.curriculum.mapping.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign
import world.respect.shared.generated.resources.copy_playlist
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.share
import world.respect.shared.viewmodel.curriculum.mapping.detail.CurriculumMappingDetailUiState
import world.respect.shared.viewmodel.curriculum.mapping.detail.CurriculumMappingDetailViewModel
import world.respect.shared.viewmodel.curriculum.mapping.detail.CurriculumMappingLessonUiState
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSection
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSectionLink

@Composable
fun CurriculumMappingDetailScreen(
    uiState: CurriculumMappingDetailUiState = CurriculumMappingDetailUiState(),
    lessonUiStateFor: (CurriculumMappingSectionLink) -> Flow<DataLoadState<CurriculumMappingLessonUiState>>,
    onClickEdit: () -> Unit = {},
    onClickShare: () -> Unit = {},
    onClickCopyPlaylist: () -> Unit = {},
    onClickAssign: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickLesson: (String) -> Unit = {},
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClickEdit,
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                MappingHeaderCard(
                    mapping = uiState.mapping,
                    onClickShare = onClickShare,
                    onClickCopyPlaylist = onClickCopyPlaylist,
                    onClickAssign = onClickAssign,
                    onClickDelete = onClickDelete
                )
            }

            uiState.mapping?.sections?.forEach { section ->
                item(key = "section_${section.uid}") {
                    SectionHeader(section = section)
                }

                items(
                    items = section.items,
                    key = { link -> link.href }
                ) { link ->
                    LessonItem(
                        link = link,
                        lessonUiStateFor = lessonUiStateFor,
                        onClickLesson = onClickLesson
                    )
                }
            }
        }
    }
}

@Composable
private fun MappingHeaderCard(
    mapping: CurriculumMapping?,
    onClickShare: () -> Unit,
    onClickCopyPlaylist: () -> Unit,
    onClickAssign: () -> Unit,
    onClickDelete: () -> Unit
) {
    if (mapping == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.Book,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = mapping.description.orEmpty(),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!mapping.subject.isNullOrEmpty()) {
                            Text(
                                text = mapping.subject!!,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!mapping.grade.isNullOrEmpty()) {
                            Text(
                                text = mapping.grade!!,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!mapping.language.isNullOrEmpty()) {
                            Text(
                                text = mapping.language!!,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Outlined.Share,
                    label = stringResource(Res.string.share),
                    onClick = onClickShare
                )
                ActionButton(
                    icon = Icons.Outlined.ContentCopy,
                    label = stringResource(Res.string.copy_playlist),
                    onClick = onClickCopyPlaylist
                )
                ActionButton(
                    icon = Icons.Outlined.Task,
                    label = stringResource(Res.string.assign),
                    onClick = onClickAssign
                )
                ActionButton(
                    icon = Icons.Outlined.Delete,
                    label = stringResource(Res.string.delete),
                    onClick = onClickDelete
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(section: CurriculumMappingSection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = section.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Outlined.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LessonItem(
    link: CurriculumMappingSectionLink,
    lessonUiStateFor: (CurriculumMappingSectionLink) -> Flow<DataLoadState<CurriculumMappingLessonUiState>>,
    onClickLesson: (String) -> Unit
) {
    val stateFlow = remember(link.href) {
        lessonUiStateFor(link)
    }

    val lessonUiState by stateFlow.collectAsState(initial = DataLoadingState())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickLesson(link.href) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        lessonUiState.dataOrNull()?.icon?.also { iconUrl ->
            link.title?.let {
                RespectAsyncImage(
                    uri = iconUrl.toString(),
                    contentDescription = it,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp)
                )
            }
        } ?: run {
            Icon(
                Icons.Filled.Book,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = link.title.orEmpty(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CurriculumMappingDetailScreenForViewModel(
    viewModel: CurriculumMappingDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CurriculumMappingDetailScreen(
        uiState = uiState,
        lessonUiStateFor = viewModel::lessonUiStateFor,
        onClickEdit = viewModel::onClickEdit,
        onClickShare = viewModel::onClickShare,
        onClickCopyPlaylist = viewModel::onClickCopyPlaylist,
        onClickAssign = viewModel::onClickAssign,
        onClickDelete = viewModel::onClickDelete,
        onClickLesson = viewModel::onClickLesson
    )
}