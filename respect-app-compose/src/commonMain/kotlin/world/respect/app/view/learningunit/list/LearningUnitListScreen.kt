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
import androidx.compose.material.icons.filled.Task
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.RespectQuickActionButton
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.copy
import world.respect.shared.generated.resources.copy_of_playlist
import world.respect.shared.generated.resources.copy_playlist
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.duration
import world.respect.shared.generated.resources.make_a_copy
import world.respect.shared.generated.resources.name
import world.respect.shared.generated.resources.permanently_delete
import world.respect.shared.generated.resources.permanently_delete_description
import world.respect.shared.generated.resources.select_count_items
import world.respect.shared.generated.resources.select_playlist
import world.respect.shared.generated.resources.share
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListUiState
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel.Companion.ICON

@Composable
fun LearningUnitListScreen(
    viewModel: LearningUnitListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val copyOfTemplate = stringResource(Res.string.copy_of_playlist)

    LaunchedEffect(uiState.showCopyDialog) {
        if (uiState.showCopyDialog) {
            viewModel.onCopyDialogNameChanged(copyOfTemplate.format(uiState.copyDialogName))
        }
    }

    LearningUnitListScreen(
        uiState = uiState,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onClickPublication = viewModel::onClickPublication,
        onLongPressPublication = viewModel::onLongPressPublication,
        onClickNavigation = viewModel::onClickNavigation,
        onClickConfirmSelection = viewModel::onClickConfirmSelection,
        onClickSelectPlaylist = viewModel::onClickSelectPlaylist,
        onClickToggleSection = viewModel::onClickToggleSection,
        onClickShare = viewModel::onClickShare,
        onClickCopy = viewModel::onClickCopy,
        onClickDelete = viewModel::onClickDelete,
        onClickAssignSection = viewModel::onClickAssignSection,
        onCopyDialogDismiss = viewModel::onCopyDialogDismiss,
        onCopyDialogNameChanged = viewModel::onCopyDialogNameChanged,
        onCopyDialogConfirm = viewModel::onCopyDialogConfirm,
        onDeleteDialogDismiss = viewModel::onDeleteDialogDismiss,
        onDeleteDialogConfirm = viewModel::onDeleteDialogConfirm,
    )
}

@Composable
fun LearningUnitListScreen(
    uiState: LearningUnitListUiState = LearningUnitListUiState(),
    @Suppress("unused") onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickPublication: (OpdsPublication) -> Unit = {},
    onLongPressPublication: (OpdsPublication) -> Unit = {},
    onClickNavigation: (ReadiumLink) -> Unit = {},
    onClickConfirmSelection: () -> Unit = {},
    onClickSelectPlaylist: () -> Unit = {},
    onClickToggleSection: (String) -> Unit = {},
    onClickShare: () -> Unit = {},
    onClickCopy: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickAssignSection: (Int) -> Unit = {},
    onCopyDialogDismiss: () -> Unit = {},
    onCopyDialogNameChanged: (String) -> Unit = {},
    onCopyDialogConfirm: () -> Unit = {},
    onDeleteDialogDismiss: () -> Unit = {},
    onDeleteDialogConfirm: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = if (
                uiState.showSelectPlaylistButton ||
                (uiState.isMultiSelectMode && uiState.selectedCount > 0)
            ) {
                PaddingValues(bottom = 72.dp)
            } else {
                PaddingValues()
            },
        ) {
            item(key = "feed_header") {
                FeedHeader(
                    uiState = uiState,
                    onClickShare = onClickShare,
                    onClickCopy = onClickCopy,
                    onClickDelete = onClickDelete,
                    onClickAssign = {
                        onClickAssignSection(
                            LearningUnitListViewModel.ASSIGN_HEADER_SECTION_INDEX
                        )
                    },
                )
                HorizontalDivider()
            }

            itemsIndexed(
                items = uiState.navigation,
                key = { index, _ -> "top_nav_$index" }
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
                key = { index, _ -> "top_pub_$index" }
            ) { _, publication ->
                PublicationListItem(
                    publication = publication,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    isSelected = uiState.isPublicationSelected(publication),
                    onClickPublication = { onClickPublication(publication) },
                    onLongPressPublication = { onLongPressPublication(publication) },
                )
            }

            uiState.group.forEachIndexed { sectionIndex, group ->
                item(key = "section_$sectionIndex") {
                    FeedSectionHeader(
                        title = group.metadata.title,
                        isCollapsed = uiState.isSectionCollapsed(sectionIndex.toString()),
                        showAssignButton = group.publications?.isNotEmpty() == true,
                        onClickToggle = { onClickToggleSection(sectionIndex.toString()) },
                        onClickAssign = { onClickAssignSection(sectionIndex) },
                    )
                }

                if (!uiState.isSectionCollapsed(sectionIndex.toString())) {
                    itemsIndexed(
                        items = group.navigation ?: emptyList(),
                        key = { itemIndex, _ -> "nav_${sectionIndex}_$itemIndex" }
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
                        key = { itemIndex, _ -> "pub_${sectionIndex}_$itemIndex" }
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
        }

        if (uiState.showSelectPlaylistButton) {
            Button(
                onClick = onClickSelectPlaylist,
                enabled = uiState.selectedNavigation != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .defaultItemPadding()
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
                    .defaultItemPadding()
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

    if (uiState.showCopyDialog) {
        CopyFeedDialog(
            name = uiState.copyDialogName,
            onNameChanged = onCopyDialogNameChanged,
            onDismiss = onCopyDialogDismiss,
            onConfirm = onCopyDialogConfirm,
        )
    }

    if (uiState.showDeleteDialog) {
        DeleteFeedDialog(
            onDismiss = onDeleteDialogDismiss,
            onConfirm = onDeleteDialogConfirm,
        )
    }
}

@Composable
private fun FeedHeader(
    uiState: LearningUnitListUiState,
    onClickShare: () -> Unit,
    onClickCopy: () -> Unit,
    onClickDelete: () -> Unit,
    onClickAssign: () -> Unit,
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
                        uri = uiState.group
                            .flatMap { it.publications ?: emptyList() }
                            .firstOrNull()
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
                .defaultItemPadding(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            RespectQuickActionButton(
                modifier = Modifier.testTag("share_btn"),
                labelText = stringResource(Res.string.share),
                iconContent = {
                    Icon(Icons.Filled.Share, null)
                },
                onClick = onClickShare
            )

            RespectQuickActionButton(
                modifier = Modifier.testTag("copy_btn"),
                labelText = stringResource(Res.string.copy_playlist),
                iconContent = {
                    Icon(Icons.Filled.ContentCopy, null)
                },
                onClick = onClickCopy
            )

            RespectQuickActionButton(
                modifier = Modifier.testTag("header_assign_btn"),
                labelText = stringResource(Res.string.assign),
                iconContent = {
                    Icon(Icons.Filled.Task, null)
                },
                onClick = onClickAssign
            )

            if (uiState.isTeacherOrAdmin) {
                RespectQuickActionButton(
                    modifier = Modifier.testTag("delete_btn"),
                    labelText = stringResource(Res.string.delete),
                    iconContent = {
                        Icon(Icons.Filled.Delete, null)
                    },
                    onClick = onClickDelete,
                )
            }
        }
    }
}

@Composable
private fun FeedSectionHeader(
    title: String,
    isCollapsed: Boolean,
    showAssignButton: Boolean,
    onClickToggle: () -> Unit,
    onClickAssign: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showAssignButton) {
                    IconButton(
                        onClick = onClickAssign,
                        modifier = Modifier.testTag("assign_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Task,
                            contentDescription = stringResource(Res.string.assign),
                        )
                    }
                }
                IconButton(
                    onClick = onClickToggle,
                    modifier = Modifier.testTag("expand_collapse_icon"),
                ) {
                    Icon(
                        imageVector = if (isCollapsed) {
                            Icons.Filled.ExpandMore
                        } else {
                            Icons.Filled.ExpandLess
                        },
                        contentDescription = null,
                    )
                }
            }
        },
    )
}

@Composable
private fun CopyFeedDialog(
    name: String,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.make_a_copy)) },
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
                Text(text = stringResource(Res.string.copy))
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
private fun DeleteFeedDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Filled.Delete, contentDescription = null) },
        title = { Text(text = stringResource(Res.string.permanently_delete)) },
        text = { Text(text = stringResource(Res.string.permanently_delete_description)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("delete_dialog_confirm"),
            ) {
                Text(text = stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("delete_dialog_dismiss"),
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
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