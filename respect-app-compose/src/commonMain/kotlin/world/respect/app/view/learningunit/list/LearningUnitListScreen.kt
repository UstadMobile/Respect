@file:Suppress("UNCHECKED_CAST")

package world.respect.app.view.learningunit.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.langMapString
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.duration
import world.respect.shared.generated.resources.select_count_items
import world.respect.shared.generated.resources.select_playlist
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListUiState
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel.Companion.ICON

@Composable
fun LearningUnitListScreen(
    viewModel: LearningUnitListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    LearningUnitListScreen(
        uiState = uiState,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onClickPublication = viewModel::onClickPublication,
        onLongPressPublication = viewModel::onLongPressPublication,
        onClickNavigation = viewModel::onClickNavigation,
        onClickConfirmSelection = viewModel::onClickConfirmSelection,
        onClickSelectPlaylist = viewModel::onClickSelectPlaylist,
    )
}

@Composable
fun LearningUnitListScreen(
    uiState: LearningUnitListUiState,
    @Suppress("unused") onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickPublication: (OpdsPublication) -> Unit,
    onLongPressPublication: (OpdsPublication) -> Unit = {},
    onClickNavigation: (ReadiumLink) -> Unit,
    onClickConfirmSelection: () -> Unit = {},
    onClickSelectPlaylist: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = if (uiState.showSelectPlaylistButton || (uiState.isMultiSelectMode && uiState.selectedCount > 0)) {
                PaddingValues(bottom = 72.dp)
            } else {
                PaddingValues()
            },
        ) {
            itemsIndexed(
                items = uiState.navigation,
                key = { _, navigation -> navigation.href }
            ) { _, navigation ->
                NavigationListItem(
                    navigation = navigation,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    isSelected = uiState.isNavigationSelected(navigation),
                    onClickNavigation = { onClickNavigation(navigation) },
                )
            }

            itemsIndexed(
                items = uiState.publications,
                key = { _, publication -> publication.metadata.identifier.toString() }
            ) { _, publication ->
                PublicationListItem(
                    publication = publication,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    isSelected = uiState.isPublicationSelected(publication),
                    onClickPublication = { onClickPublication(publication) },
                    onLongPressPublication = { onLongPressPublication(publication) },
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
                    key = { _, navigation -> navigation.href }
                ) { _, navigation ->
                    NavigationListItem(
                        navigation = navigation,
                        isMultiSelectMode = uiState.isMultiSelectMode,
                        isSelected = uiState.isNavigationSelected(navigation),
                        onClickNavigation = { onClickNavigation(navigation) },
                    )
                }

                itemsIndexed(
                    items = group.publications ?: emptyList(),
                    key = { _, publication -> publication.metadata.identifier.toString() }
                ) { _, publication ->
                    PublicationListItem(
                        publication = publication,
                        isMultiSelectMode = uiState.isMultiSelectMode,
                        isSelected = uiState.isPublicationSelected(publication),
                        onClickPublication = { onClickPublication(publication) },
                        onLongPressPublication = { onLongPressPublication(publication) },
                    )
                }
            }
        }

        if (uiState.showSelectPlaylistButton) {
            Button(
                onClick = onClickSelectPlaylist,
                enabled = uiState.selectedNavigation != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("select_playlist_button"),
            ) {
                Text(text = stringResource(Res.string.select_playlist))
            }
        } else if (uiState.isMultiSelectMode && uiState.selectedCount > 0) {
            Button(
                onClick = onClickConfirmSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("confirm_selection_button"),
            ) {
                Text(
                    text = stringResource(
                        Res.string.select_count_items,
                        uiState.selectedCount,
                    ),
                )
            }
        }

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedListItem(
    title: String,
    iconUrl: String?,
    description: String?,
    language: List<String>?,
    duration: Double?,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress,
            ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                RespectAsyncImage(
                    uri = iconUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(36.dp),
                )
            }
        },
        headlineContent = {
            Text(text = title)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                description?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    language?.let { Text(text = it.joinToString(", ")) }
                    duration?.let {
                        Text(text = "${stringResource(Res.string.duration)} - $it")
                    }
                }
            }
        },
        trailingContent = if (isMultiSelectMode) {
            {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    modifier = Modifier.testTag("check_box"),
                )
            }
        } else {
            null
        },
    )
}

@Composable
fun NavigationListItem(
    navigation: ReadiumLink,
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onClickNavigation: (ReadiumLink) -> Unit,
) {
    FeedListItem(
        title = navigation.title
            ?.takeIf { it != "null" && it.isNotBlank() }
            ?: navigation.href,
        iconUrl = navigation.alternate?.find {
            it.rel?.contains(ICON) == true
        }?.href,
        description = null,
        language = navigation.language,
        duration = navigation.duration,
        isMultiSelectMode = isMultiSelectMode,
        isSelected = isSelected,
        onClick = { onClickNavigation(navigation) },
        onLongPress = {},
    )
}

@Composable
fun PublicationListItem(
    publication: OpdsPublication,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClickPublication: (OpdsPublication) -> Unit,
    onLongPressPublication: (OpdsPublication) -> Unit,
) {
    FeedListItem(
        title = langMapString(publication.metadata.title),
        iconUrl = publication.images?.firstOrNull()?.href,
        language = publication.metadata.language,
        duration = publication.metadata.duration,
        description = publication.metadata.description,
        isMultiSelectMode = isMultiSelectMode,
        isSelected = isSelected,
        onClick = { onClickPublication(publication) },
        onLongPress = { onLongPressPublication(publication) },
    )
}