package world.respect.app.view.learningunit.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.theme.black
import com.ustadmobile.libuicompose.theme.white
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.score_or_progress
import world.respect.shared.generated.resources.app_name
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.vector.ImageVector
import world.respect.shared.generated.resources.assign
import world.respect.shared.generated.resources.download
import world.respect.shared.generated.resources.play
import world.respect.shared.generated.resources.share
import world.respect.shared.viewmodel.app.appstate.getTitle
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.layout.ContentScale
import world.respect.app.app.RespectAsyncImage
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailUiState
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel.Companion.IMAGE

@Composable
fun LearningUnitDetailScreen(
    viewModel: LearningUnitDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LearningUnitDetailScreen(
        uiState = uiState
    )
}

@Composable
fun LearningUnitDetailScreen(
    uiState: LearningUnitDetailUiState,
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
                    val iconUrl = uiState.lessonDetail?.images?.find {
                        it.type?.contains(IMAGE) == true
                    }?.href

                    iconUrl.also { icon ->
                        RespectAsyncImage(
                            uri = icon,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)

                        )
                    }
                },
                headlineContent = {
                    Text(
                        text = uiState.lessonDetail?.metadata?.title?.getTitle().orEmpty(),
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingContent = {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
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

                        Text(
                            text = stringResource(Res.string.score_or_progress),
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
                IconLabel(
                    Icons.Filled.ArrowCircleDown,
                    stringResource(Res.string.download)
                )
                IconLabel(
                    Icons.Filled.Share,
                    stringResource(Res.string.share)
                )
                IconLabel(
                    Icons.Filled.NearMe,
                    stringResource(Res.string.assign)
                )
            }
        }
    }
}

@Composable
private fun IconLabel(icon: ImageVector, labelRes: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier
                .height(4.dp)
        )

        Text(
            text = labelRes
        )

    }
}
