package world.respect.app.view.learningunit.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.libuicompose.theme.black
import com.ustadmobile.libuicompose.theme.white
import org.jetbrains.compose.resources.stringResource
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.score_or_progress
import respect.composeapp.generated.resources.app_name
import world.respect.app.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.vector.ImageVector
import respect.composeapp.generated.resources.assign
import respect.composeapp.generated.resources.clazz
import respect.composeapp.generated.resources.download
import respect.composeapp.generated.resources.duration
import respect.composeapp.generated.resources.play
import respect.composeapp.generated.resources.related_lessons
import respect.composeapp.generated.resources.share
import world.respect.app.appstate.getTitle
import world.respect.app.appstate.toDisplayString
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.layout.ContentScale
import world.respect.app.app.RespectAsyncImage
import world.respect.app.viewmodel.learningunit.detail.LearningUnitDetailUiState
import world.respect.datasource.opds.model.OpdsPublication

@Composable
fun LearningUnitDetailScreen(
    viewModel: LearningUnitDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LearningUnitDetailScreen(
        uiState = uiState,
        onClickLesson = { viewModel.onClickLesson(it) },
        )
}

@Composable
fun LearningUnitDetailScreen(
    uiState: LearningUnitDetailUiState,
    onClickLesson: (OpdsPublication) -> Unit,
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
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("", color = Color.White)
                    }
                },
                headlineContent = {
                    Text(
                        text = uiState.lessonDetail?.metadata?.title?.getTitle().orEmpty(),
                        fontWeight = FontWeight.Bold

                    )
                },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = uiState.lessonDetail?.metadata?.subtitle?.getTitle()
                                .orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = stringResource(Res.string.score_or_progress),
                            style = MaterialTheme.typography.bodySmall
                        )

                        LinearProgressIndicator(
                            progress = { 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                    }
                }
            )
        }

        item {
            OutlinedButton(
                onClick = { /* Play */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(stringResource(Res.string.play))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconLabel(Icons.Filled.ArrowCircleDown, stringResource(Res.string.download))
                IconLabel(Icons.Filled.Share, stringResource(Res.string.share))
                IconLabel(Icons.Filled.NearMe, stringResource(Res.string.assign))
            }
        }

        item {
            Text(
                text = stringResource(Res.string.related_lessons),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        items(uiState.publications) { publication ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickLesson(publication) },
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${stringResource(Res.string.duration)} - ${publication.metadata.duration}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun IconLabel(icon: ImageVector, labelRes: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = labelRes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

    }
}
