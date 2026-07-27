package world.respect.app.view.learningunit.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libuicompose.theme.black
import com.ustadmobile.libuicompose.theme.white
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.RespectDataLoadHost
import world.respect.app.components.RespectOfflineItemStatusIcon
import world.respect.app.components.RespectQuickActionButton
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.app.components.uiTextStringResource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.name
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.download
import world.respect.shared.generated.resources.downloaded
import world.respect.shared.generated.resources.license
import world.respect.shared.generated.resources.open
import world.respect.shared.generated.resources.subject
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailUiState
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun LearningUnitDetailScreen(
    viewModel: LearningUnitDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LearningUnitDetailScreen(
        uiState = uiState,
        onClickOpen = viewModel::onClickOpen,
        onClickDownload = viewModel::onClickDownload,
        onClickAssign = viewModel::onClickAssign,
        onClickApp = viewModel::onClickApp,
        onClickLicense = viewModel::onClickLicense,
    )
}

@Composable
fun LearningUnitDetailScreen(
    uiState: LearningUnitDetailUiState,
    onClickOpen: () -> Unit,
    onClickDownload: () -> Unit,
    onClickAssign: () -> Unit,
    onClickApp: (OpdsPublication) -> Unit,
    onClickLicense: (OpdsPublication) -> Unit,
) {
    RespectDataLoadHost(
        uiState.lessonDetail,
        modifier = Modifier.fillMaxSize().padding(vertical = 10.dp)
    ) {
        val lessonDetail = uiState.lessonDetail.dataOrNull()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ListItem(
                leadingContent = {
                    val iconUrl = lessonDetail?.images?.firstOrNull()?.href

                    iconUrl?.also { icon ->
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
                        text = lessonDetail?.metadata?.title?.let { langMapString(it) } ?: "",
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingContent = {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                uiState.appDetail?.let {
                                    onClickApp(
                                        it
                                    )
                                }
                            }

                        ) {

                            val appIconUrl = uiState.appDetail?.images?.firstOrNull()?.href
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(white)
                                    .border(1.dp, black, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (appIconUrl != null) {
                                    RespectAsyncImage(
                                        uri = appIconUrl,
                                        contentDescription = "",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = uiState.appDetail?.metadata?.title?.let {
                                    langMapString(
                                        it
                                    )
                                }
                                    ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        val duration =
                            lessonDetail?.links?.firstOrNull { it.duration != null }?.duration?.seconds
                        if (duration != null) {
                            Text(
                                text = duration.toString(),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                    }
                }
            )

            Button(
                onClick = {
                    onClickOpen()
                },
                enabled = uiState.buttonsEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.open))

            }
            HorizontalDivider()
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
                        RespectOfflineItemStatusIcon(
                            state = uiState.pinState,
                        )
                    },
                    onClick = onClickDownload,
                    enabled = uiState.buttonsEnabled,
                )

                if (uiState.showAssignButton) {
                    RespectQuickActionButton(
                        imageVector = Icons.Filled.NearMe,
                        labelText = stringResource(Res.string.assign),
                        onClick = onClickAssign,
                        enabled = uiState.buttonsEnabled,
                    )
                }
            }

            HorizontalDivider()

            FlowRow(
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.licenseLabel?.let { label ->
                    Tag(
                        text = "${stringResource(Res.string.license)}: ${
                            uiTextStringResource(
                                label
                            )
                        }",
                        onTagClick = { uiState.appDetail?.let { onClickLicense(it) } }
                    )
                }

                lessonDetail?.metadata?.subject?.forEach { subject ->
                    Tag(
                        text = "${stringResource(Res.string.subject)}: ${langMapString(subject.name)}",
                    )
                }
            }
        }
    }
}


@Composable
private fun Tag(text: String, onTagClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .clickable(enabled = onTagClick != {}, onClick = onTagClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
