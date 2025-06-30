package world.respect.app.view.lessons.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.libuicompose.theme.black
import org.jetbrains.compose.resources.stringResource
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.clazz
import respect.composeapp.generated.resources.duration
import world.respect.app.app.RespectAsyncImage
import world.respect.app.appstate.getTitle
import world.respect.app.appstate.toDisplayString
import world.respect.app.viewmodel.lessons.list.LessonListUiState
import world.respect.app.viewmodel.lessons.list.LessonListViewModel

@Composable
fun LessonListScreen(
    viewModel: LessonListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LessonListScreen(
        uiState = uiState,
        onClickLesson = { viewModel.onClickLesson() },
        onClickFilter = { viewModel.onClickFilter(it) }

    )
}

@Composable
fun LessonListScreen(
    uiState: LessonListUiState,
    onClickLesson: () -> Unit,
    onClickFilter: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        uiState.lessonFilter.firstOrNull()?.let { facet ->
            var expanded by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .border(1.dp, black, shape = RoundedCornerShape(6.dp))
                        .clip(CircleShape)
                        .clickable { expanded = true }
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = uiState.selectedFilterTitle ?: (facet.metadata?.title ?: ""))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            modifier = Modifier.padding(6.dp),
                            contentDescription = null
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = facet.metadata?.title ?: "",
                                fontSize = 14.sp
                            )
                        },
                        onClick = {},
                        enabled = false
                    )

                    facet.links.forEach { link ->
                        DropdownMenuItem(
                            text = { Text(link.title ?: "") },
                            onClick = {
                                onClickFilter(link.title ?: "")
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(uiState.publications) { publication ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClickLesson() },

                    leadingContent = {
                        RespectAsyncImage(
                            uri = "",
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                        )
                    },

                    headlineContent = {
                        Text(
                            text = publication.metadata.title.getTitle(),
                            style = MaterialTheme.typography.titleSmall
                        )
                    },

                    supportingContent = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(Res.string.clazz),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = publication.metadata.subject
                                        ?.joinToString(", ") { it.toDisplayString() }
                                        ?: " ",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    text = "${stringResource(Res.string.duration)} - ${publication.metadata.duration}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    },

                    trailingContent = {
                        Icon(
                            imageVector = Icons.Filled.ArrowCircleDown,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }

}