package world.respect.app.view.lessonslist.downloaded

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.delete
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.lessonslist.downloaded.DownloadedLessonItem
import world.respect.shared.viewmodel.lessonslist.downloaded.DownloadedLessonsUiState
import world.respect.shared.viewmodel.lessonslist.downloaded.DownloadedLessonsViewModel

@Composable
fun DownloadedLessonsScreen(
    viewModel: DownloadedLessonsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    DownloadedLessonsScreen(
        uiState = uiState,
        onClickLesson = viewModel::onClickLesson,
        onClickDelete = viewModel::onClickDelete,
    )
}

@Composable
fun DownloadedLessonsScreen(
    uiState: DownloadedLessonsUiState,
    onClickLesson: (DownloadedLessonItem) -> Unit,
    onClickDelete: (DownloadedLessonItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        items(
            items = uiState.lessons,
            key = { it.url }
        ) { lesson ->
            DownloadedLessonListItem(
                lesson = lesson,
                onClick = { onClickLesson(lesson) },
                onClickDelete = { onClickDelete(lesson) }
            )
        }
    }
}

@Composable
private fun DownloadedLessonListItem(
    lesson: DownloadedLessonItem,
    onClick: () -> Unit,
    onClickDelete: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = lesson.publication?.metadata?.title?.getTitle().orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                lesson.publication?.metadata?.subtitle?.getTitle()?.let { subtitle ->
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val metadataItems = buildList {
                    lesson.publication?.metadata?.language?.firstOrNull()?.let { lang ->
                        add(lang)
                    }

                    lesson.publication?.metadata?.subject?.firstOrNull()?.let { subject ->
                        add(subject)
                    }
                }

                if (metadataItems.isNotEmpty()) {
                    Text(
                        text = metadataItems.joinToString(separator = " • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        leadingContent = {
            val iconUrl = lesson.publication?.images?.firstOrNull()?.href

            Box(
                modifier = Modifier.size(64.dp)
            ) {
                RespectAsyncImage(
                    uri = iconUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        trailingContent = {
            IconButton(onClick = onClickDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}