package world.respect.app.view.apps.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.datalayer.DataReadyState
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.findIcons
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_app
import world.respect.shared.generated.resources.lessons
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.apps.detail.AppsDetailUiState
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel.Companion.APP_DETAIL
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel.Companion.BUTTONS_ROW
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel.Companion.LEARNING_UNIT_LIST
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel.Companion.LESSON_HEADER

@Composable
fun AppsDetailScreen(
    viewModel: AppsDetailViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    AppsDetailScreen(
        uiState = uiState,
        onClickLessonList = { viewModel.onClickLessonList() },
        onClickPublication = { viewModel.onClickPublication(it) },
        onClickNavigation = { viewModel.onClickNavigation(it) },
        onClickAdd = { viewModel.onClickAdd() }
    )
}

@Composable
fun AppsDetailScreen(
    uiState: AppsDetailUiState,
    onClickLessonList: () -> Unit,
    onClickPublication: (OpdsPublication) -> Unit,
    onClickNavigation: (ReadiumLink) -> Unit,
    onClickAdd: () -> Unit
) {

    val appDetail = (uiState.appDetail as? DataReadyState)?.data

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(
            key = APP_DETAIL
        ) {
            ListItem(
                leadingContent = {
                    appDetail?.findIcons()?.firstOrNull()?.also {
                        RespectAsyncImage(
                            uri = it.href,
                            contentDescription = "",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                },
                headlineContent = {
                    Text(
                        text = appDetail?.metadata?.title?.getTitle() ?: ""
                    )
                },
                supportingContent = {
                    Text(
                        text = appDetail?.metadata?.subtitle?.getTitle() ?: "",
                        maxLines = 1
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item(
            key = BUTTONS_ROW
        ) {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                if(!uiState.isAdded && uiState.showAddRemoveButton) {
                    OutlinedButton(
                        onClick = onClickAdd,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.add_app))
                    }
                }
            }
        }

        item(
            key = LESSON_HEADER
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.lessons),
                        fontWeight = FontWeight.Bold
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickLessonList() }
            )
        }
        item(key = LEARNING_UNIT_LIST) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(
                    items = uiState.navigation,
                    key = { _, navigation ->
                        navigation.href
                    }
                ) { _, navigation ->
                    NavigationList(
                        navigation,
                        onClickNavigation = {
                            onClickNavigation(navigation)
                        }
                    )
                }

                itemsIndexed(
                    items = uiState.publications,
                    key = { _, publication ->
                        publication.metadata.identifier.toString()
                    }
                ) { _, publication ->
                    PublicationList(
                        publication,
                        onClickPublication = {
                            onClickPublication(publication)
                        }
                    )
                }

                uiState.group.forEach { _ ->
                    itemsIndexed(
                        items = uiState.navigation,
                        key = { _, navigation ->
                            navigation.href
                        }
                    ) { _, navigation ->
                        NavigationList(
                            navigation,
                            onClickNavigation = {
                                onClickNavigation(navigation)
                            }
                        )
                    }

                    itemsIndexed(
                        items = uiState.publications,
                        key = { _, publication ->
                            publication.metadata.identifier.toString()
                        }
                    ) { _, publication ->
                        PublicationList(
                            publication,
                            onClickPublication = {
                                onClickPublication(publication)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationList(
    navigation: ReadiumLink,
    onClickNavigation: (ReadiumLink) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable {
                onClickNavigation(navigation)
            }
    ) {
        val iconUrl = navigation.alternate?.find {
            it.rel?.contains("icon") == true
        }?.href

        iconUrl.also { icon ->
            RespectAsyncImage(
                uri = icon,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = navigation.title.toString(),
            maxLines = 3,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PublicationList(
    publication: OpdsPublication, onClickPublication: (OpdsPublication) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClickPublication(publication) }
    ) {
        val iconUrl = publication.images?.firstOrNull()?.href

        iconUrl.also { icon ->
            RespectAsyncImage(
                uri = icon,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))

            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = publication.metadata.title.getTitle(),
            maxLines = 1,
        )
    }
}
