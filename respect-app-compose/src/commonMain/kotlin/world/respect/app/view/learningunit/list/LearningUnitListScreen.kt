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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.copy_playlist
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.duration
import world.respect.shared.generated.resources.make_a_copy
import world.respect.shared.generated.resources.name
import world.respect.shared.generated.resources.select_count_items
import world.respect.shared.generated.resources.share
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListUiState
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel.Companion.ICON
import world.respect.shared.viewmodel.learningunit.list.PlaylistDetailViewModel

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
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = if (uiState.isMultiSelectMode && uiState.selectedCount > 0) {
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
                        headlineContent = {
                            Text(text = group.metadata.title)
                        }
                    )
                }

                itemsIndexed(
                    items = group.navigation ?: emptyList(),
                    key = { _, navigation -> navigation.href }
                ) { _, navigation ->
                    NavigationListItem(
                        navigation = navigation,
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

        if (uiState.isMultiSelectMode && uiState.selectedCount > 0) {
            Button(
                onClick = onClickConfirmSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("confirm_selection_button"),
            ) {
                Text(
                    text = stringResource(Res.string.select_count_items, uiState.selectedCount),
                )
            }
        }
    }
}

@Composable
fun PlaylistDetailScreenForViewModel(
    viewModel: PlaylistDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    PlaylistDetailScreen(
        uiState = uiState,
        onClickToggleSection = viewModel::onClickToggleSection,
        onClickShare = viewModel::onClickShare,
        onClickCopyPlaylist = viewModel::onClickCopyPlaylist,
        onClickDelete = viewModel::onClickDelete,
        onClickPublication = {},
        onClickNavigation = {},
        onCopyDialogDismiss = viewModel::onCopyDialogDismiss,
        onCopyDialogNameChanged = viewModel::onCopyDialogNameChanged,
        onCopyDialogConfirm = viewModel::onCopyDialogConfirm,
    )
}

@Composable
fun PlaylistDetailScreen(
    uiState: LearningUnitListUiState,
    onClickToggleSection: (String) -> Unit,
    onClickShare: () -> Unit,
    onClickCopyPlaylist: () -> Unit,
    onClickDelete: () -> Unit,
    onClickPublication: (OpdsPublication) -> Unit,
    onClickNavigation: (ReadiumLink) -> Unit,
    onCopyDialogDismiss: () -> Unit = {},
    onCopyDialogNameChanged: (String) -> Unit = {},
    onCopyDialogConfirm: () -> Unit = {},
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            PlaylistDetailHeader(
                uiState = uiState,
                onClickShare = onClickShare,
                onClickCopyPlaylist = onClickCopyPlaylist,
                onClickDelete = onClickDelete,
            )
            HorizontalDivider()
        }

        uiState.group.forEach { group ->
            item(key = "section_${group.metadata.title}") {
                PlaylistSectionHeader(
                    title = group.metadata.title,
                    isCollapsed = uiState.isSectionCollapsed(group.metadata.title),
                    onClickToggle = { onClickToggleSection(group.metadata.title) },
                )
            }

            if (!uiState.isSectionCollapsed(group.metadata.title)) {
                itemsIndexed(
                    items = group.navigation ?: emptyList(),
                    key = { _, navigation ->
                        "nav_${group.metadata.title}_${navigation.href}"
                    }
                ) { _, navigation ->
                    NavigationListItem(
                        navigation = navigation,
                        onClickNavigation = { onClickNavigation(navigation) },
                    )
                }
                itemsIndexed(
                    items = group.publications ?: emptyList(),
                    key = { _, publication ->
                        "pub_${group.metadata.title}_${publication.metadata.identifier}"
                    }
                ) { _, publication ->
                    PublicationListItem(
                        publication = publication,
                        isMultiSelectMode = false,
                        isSelected = false,
                        onClickPublication = { onClickPublication(publication) },
                        onLongPressPublication = {},
                    )
                }
            }
        }
    }

    if (uiState.showCopyDialog) {
        CopyPlaylistDialog(
            name = uiState.copyDialogName,
            onNameChanged = onCopyDialogNameChanged,
            onDismiss = onCopyDialogDismiss,
            onConfirm = onCopyDialogConfirm,
        )
    }
}

@Composable
private fun CopyPlaylistDialog(
    name: String,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(Res.string.make_a_copy))
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text(stringResource(Res.string.name)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_dialog_name_field"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("copy_dialog_confirm"),
            ) {
                Text(text = stringResource(Res.string.copy_playlist))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("copy_dialog_dismiss"),
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
private fun PlaylistDetailHeader(
    uiState: LearningUnitListUiState,
    onClickShare: () -> Unit,
    onClickCopyPlaylist: () -> Unit,
    onClickDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    RespectAsyncImage(
                        uri = uiState.publications.firstOrNull()
                            ?.images?.firstOrNull()?.href,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(36.dp),
                    )
                }
            },
            headlineContent = {
                Text(text = uiState.feed?.metadata?.description ?: "")
            },
            supportingContent = {
                uiState.feed?.metadata?.subtitle?.let { subtitle ->
                    Text(text = subtitle)
                }
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PlaylistActionButton(
                icon = Icons.Filled.Share,
                label = stringResource(Res.string.share),
                onClick = onClickShare,
            )
            PlaylistActionButton(
                icon = Icons.Filled.ContentCopy,
                label = stringResource(Res.string.copy_playlist),
                onClick = onClickCopyPlaylist,
            )
            if (uiState.isTeacherOrAdmin) {
                PlaylistActionButton(
                    icon = Icons.Filled.Delete,
                    label = stringResource(Res.string.delete),
                    onClick = onClickDelete,
                )
            }
        }
    }
}

@Composable
private fun PlaylistActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun PlaylistSectionHeader(
    title: String,
    isCollapsed: Boolean,
    onClickToggle: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        trailingContent = {
            IconButton(onClick = onClickToggle) {
                Icon(
                    imageVector = if (isCollapsed) {
                        Icons.Filled.ExpandMore
                    } else {
                        Icons.Filled.ExpandLess
                    },
                    contentDescription = null,
                )
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedListItem(
    title: String,
    iconUrl: String?,
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
                Text(text = stringResource(Res.string.classes))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    language?.let {
                        Text(text = it.joinToString(", "))
                    }
                    duration?.let {
                        Text(
                            text = "${stringResource(Res.string.duration)} - $it"
                        )
                    }
                }
            }
        },
        trailingContent = if (isMultiSelectMode) {
            {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
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
    onClickNavigation: (ReadiumLink) -> Unit,
) {
    FeedListItem(
        title = navigation.title.toString(),
        iconUrl = navigation.alternate?.find {
            it.rel?.contains(ICON) == true
        }?.href,
        language = navigation.language,
        duration = navigation.duration,
        isMultiSelectMode = false,
        isSelected = false,
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
        title = publication.metadata.title.getTitle(),
        iconUrl = publication.images?.firstOrNull()?.href,
        language = publication.metadata.language,
        duration = publication.metadata.duration,
        isMultiSelectMode = isMultiSelectMode,
        isSelected = isSelected,
        onClick = { onClickPublication(publication) },
        onLongPress = { onLongPressPublication(publication) },
    )
}