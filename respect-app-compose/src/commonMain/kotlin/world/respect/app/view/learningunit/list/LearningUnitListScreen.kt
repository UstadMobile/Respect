@file:Suppress("UNCHECKED_CAST")

package world.respect.app.view.learningunit.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.RespectListSortHeader
import world.respect.app.components.defaultItemPadding
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.duration
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListUiState
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel.Companion.ICON


@Composable
fun LearningUnitListScreen(
    viewModel: LearningUnitListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LearningUnitListScreen(
        uiState = uiState,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onClickPublication = { viewModel.onClickPublication(it) },
        onClickNavigation = { viewModel.onClickNavigation(it) },
        onClickDownloadedLesson = { viewModel.onClickDownloadedLesson(it) },
        onClickDeleteDownloaded = { viewModel.onClickDeleteDownloaded(it) }
    )
}

@Composable
fun LearningUnitListScreen(
    uiState: LearningUnitListUiState,
    @Suppress("unused") onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickPublication: (OpdsPublication) -> Unit,
    onClickNavigation: (ReadiumLink) -> Unit,
    onClickDownloadedLesson: (LearningUnitSelection) -> Unit,
    onClickDeleteDownloaded: (LearningUnitSelection) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            if (uiState.showOnlyDownloaded) {
                item(key = "sort_header") {
                    RespectListSortHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultItemPadding(),
                        sortOptions = uiState.sortOptions,
                        activeSortOrderOption = uiState.activeSortOrderOption,
                        enabled = uiState.fieldsEnabled,
                        onClickSortOption = onSortOrderChanged
                    )
                }

                itemsIndexed(
                    items = uiState.downloadedLessons,
                    key = { index, item -> item.learningUnitManifestUrl.toString() }
                ) { index, item ->
                    DownloadedLessonListItem(
                        item = item,
                        onClickLesson = { onClickDownloadedLesson(item) },
                        onClickDelete = { onClickDeleteDownloaded(item) }
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.navigation,
                    key = { index, navigation -> navigation.href }
                ) { index, navigation ->
                    NavigationListItem(
                        navigation,
                        onClickNavigation = { onClickNavigation(navigation) }
                    )
                }

                itemsIndexed(
                    items = uiState.publications,
                    key = { index, publications -> publications.metadata.identifier.toString() }
                ) { index, publication ->
                    PublicationListItem(
                        publication,
                        onClickPublication = { onClickPublication(publication) }
                    )
                }

                uiState.group.forEach { group ->
                    item {
                        ListItem(
                            headlineContent = { Text(text = group.metadata.title) }
                        )
                    }

                    itemsIndexed(
                        items = group.navigation ?: emptyList(),
                        key = { index, navigation -> navigation.href }
                    ) { index, navigation ->
                        NavigationListItem(
                            navigation,
                            onClickNavigation = { onClickNavigation(navigation) }
                        )
                    }

                    itemsIndexed(
                        items = group.publications ?: emptyList(),
                        key = { index, publication -> publication.metadata.identifier.toString() }
                    ) { index, publication ->
                        PublicationListItem(
                            publication,
                            onClickPublication = { onClickPublication(publication) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadedLessonListItem(
    item: LearningUnitSelection,
    onClickLesson: () -> Unit,
    onClickDelete: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clickable { onClickLesson() },
        leadingContent = {
            val iconUrl = item.selectedPublication.images?.firstOrNull()?.href

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                iconUrl.also { icon ->
                    RespectAsyncImage(
                        uri = icon,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        headlineContent = {
            Text(text = item.selectedPublication.metadata.title.getTitle())
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = stringResource(Res.string.classes))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.selectedPublication.metadata.language?.let { language ->
                        Text(text = language.joinToString(", "))
                    }
                    item.selectedPublication.metadata.duration?.let { duration ->
                        Text(text = "${stringResource(Res.string.duration)} - $duration")
                    }
                }
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
        }
    )
}

@Composable
fun NavigationListItem(
    navigation: ReadiumLink,
    onClickNavigation: (ReadiumLink) -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clickable {
                onClickNavigation(navigation)
            },

        leadingContent = {

            val iconUrl = navigation.alternate?.find {
                it.rel?.contains(ICON) == true
            }?.href

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                iconUrl.also { icon ->
                    RespectAsyncImage(
                        uri = icon,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
            }
        },

        headlineContent = {
            Text(
                text = navigation.title.toString()
            )
        },

        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.classes),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    navigation.language
                        ?.let { language ->
                            Text(
                                text = language.joinToString(", ")
                            )
                        }

                    navigation.duration
                        ?.let { duration ->
                            Text(
                                text = "${stringResource(Res.string.duration)} - $duration"
                            )
                        }
                }
            }
        },
    )
}

@Composable
fun PublicationListItem(
    publication: OpdsPublication, onClickPublication: (OpdsPublication) -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clickable {
                onClickPublication(publication)
            },

        leadingContent = {
            val iconUrl = publication.images?.firstOrNull()?.href

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                iconUrl.also { icon ->
                    RespectAsyncImage(
                        uri = icon,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
            }
        },

        headlineContent = {
            Text(
                text = publication.metadata.title.getTitle()
            )
        },

        supportingContent = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.classes),
                )
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    publication.metadata.language
                        ?.let { language ->
                            Text(
                                text = language.joinToString(", ")
                            )
                        }

                    publication.metadata.duration
                        ?.let { duration ->
                            Text(text = "${stringResource(Res.string.duration)} - $duration")
                        }
                }
            }
        },
    )
}