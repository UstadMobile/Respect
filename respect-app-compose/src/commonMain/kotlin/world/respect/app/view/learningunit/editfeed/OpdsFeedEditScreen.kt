package world.respect.app.view.learningunit.editfeed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.app.components.uiTextStringResource
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_section
import world.respect.shared.generated.resources.browse
import world.respect.shared.generated.resources.browse_description
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.choose_item_type
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.move
import world.respect.shared.generated.resources.move_to_section
import world.respect.shared.generated.resources.n_items
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.section_title
import world.respect.shared.generated.resources.sections
import world.respect.shared.generated.resources.title
import world.respect.shared.generated.resources.use_link
import world.respect.shared.generated.resources.use_link_description
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.learningunit.editfeed.MovingItemState
import world.respect.shared.viewmodel.learningunit.editfeed.OpdsFeedEditUiState
import world.respect.shared.viewmodel.learningunit.editfeed.OpdsFeedEditViewModel
import world.respect.shared.viewmodel.learningunit.editfeed.OpdsGroupType

@Composable
fun OpdsFeedEditScreen(
    viewModel: OpdsFeedEditViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    OpdsFeedEditScreen(
        uiState = uiState,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onGroupTitleChanged = viewModel::onSectionTitleChanged,
        onClickAddGroup = viewModel::onClickAddGroup,
        onDismissAddGroupTypeDialog = viewModel::onDismissAddGroupTypeDialog,
        onClickAddGroupType = viewModel::onClickAddGroupType,
        onClickDeleteGroup = viewModel::onClickDeleteSection,
        onGroupMoved = viewModel::onGroupMoved,
        onClickAddItem = viewModel::onClickAddItem,
        onClickAddPlaylist = viewModel::onClickAddPlaylist,
        onClickDeleteItem = viewModel::onClickDeleteItem,
        onClickMoveItem = viewModel::onClickMoveItem,
        onClickMoveItemToGroup = viewModel::onClickMoveItemToSection,
        onDismissMoveDialog = viewModel::onDismissMoveDialog,
        onGroupItemsReordered = viewModel::onGroupItemsReordered,
        onDismissAddItemTypeBottomSheet = viewModel::onDismissAddItemTypeBottomSheet,
        onClickAddItemBrowse = viewModel::onClickAddItemBrowse,
        onClickAddItemUseLink = viewModel::onClickAddItemUseLink,
    )
}

@Composable
fun OpdsFeedEditScreen(
    uiState: OpdsFeedEditUiState = OpdsFeedEditUiState(),
    onTitleChanged: (String) -> Unit = {},
    onDescriptionChanged: (String) -> Unit = {},
    onGroupTitleChanged: (groupIndex: Int, title: String) -> Unit = { _, _ -> },
    onClickAddGroup: () -> Unit = {},
    onDismissAddGroupTypeDialog: () -> Unit = {},
    onClickAddGroupType: (OpdsGroupType) -> Unit = {},
    onClickDeleteGroup: (Int) -> Unit = {},
    onGroupMoved: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onClickAddItem: (Int) -> Unit = {},
    onClickAddPlaylist: (Int) -> Unit = {},
    onClickDeleteItem: (Int, Int) -> Unit = { _, _ -> },
    onClickMoveItem: (Int, Int) -> Unit = { _, _ -> },
    onClickMoveItemToGroup: (Int) -> Unit = {},
    onDismissMoveDialog: () -> Unit = {},
    onGroupItemsReordered: (groupIndex: Int, fromIndex: Int, toIndex: Int) -> Unit = { _, _, _ -> },
    onDismissAddItemTypeBottomSheet: () -> Unit = {},
    onClickAddItemBrowse: () -> Unit = {},
    onClickAddItemUseLink: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChanged,
            label = { Text(stringResource(Res.string.title) + "*") },
            isError = uiState.titleError != null,
            supportingText = {
                Text(uiTextStringResource(uiState.titleError ?: Res.string.required.asUiText()))
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("playlist_title_field"),
            singleLine = true,
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChanged,
            label = { Text(stringResource(Res.string.description)) },
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("playlist_description_field"),
            minLines = 2,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.sections),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.defaultItemPadding(),
        )

        OutlinedButton(
            onClick = onClickAddGroup,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("add_section_button"),
        ) {
            Text(text = stringResource(Res.string.add_section))
        }

        ReorderableColumn(
            list = uiState.groups,
            onSettle = onGroupMoved,
            modifier = Modifier.fillMaxWidth(),
        ) { groupIndex, group, _ ->
            key(groupIndex) {
                ReorderableItem {
                    OpdsFeedGroupEditItem(
                        groupIndex = groupIndex,
                        group = group,
                        allGroups = uiState.groups,
                        dragHandleModifier = Modifier
                            .draggableHandle()
                            .testTag("section_drag_handle_$groupIndex"),
                        onGroupTitleChanged = { t -> onGroupTitleChanged(groupIndex, t) },
                        onClickDeleteGroup = { onClickDeleteGroup(groupIndex) },
                        onClickAddItem = { onClickAddItem(groupIndex) },
                        onClickAddPlaylist = { onClickAddPlaylist(groupIndex) },
                        onClickDeleteItem = { itemIndex ->
                            onClickDeleteItem(groupIndex, itemIndex)
                        },
                        onClickMoveItem = { itemIndex -> onClickMoveItem(groupIndex, itemIndex) },
                        onGroupItemsReordered = { fromIndex, toIndex ->
                            onGroupItemsReordered(
                                groupIndex, fromIndex, toIndex
                            )
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (uiState.addGroupTypeDialogVisible) {
        OpdsFeedEditAddGroupSelectTypeBottomSheet(
            onDismiss = onDismissAddGroupTypeDialog,
            onClickAddGroupType = onClickAddGroupType,
        )
    }

    if (uiState.isAddItemTypeBottomSheetVisible) {
        AddItemTypeBottomSheet(
            onDismiss = onDismissAddItemTypeBottomSheet,
            onClickBrowse = onClickAddItemBrowse,
            onClickUseLink = onClickAddItemUseLink,
        )
    }

    uiState.movingItem?.let { movingItem ->
        MoveToSectionDialog(
            compatibleSections = movingItem.compatibleSections,
            allSections = uiState.groups,
            onClickSection = onClickMoveItemToGroup,
            onDismiss = onDismissMoveDialog,
        )
    }
}


@Composable
fun PlaylistPublicationItemRow(
    itemIndex: Int,
    publication: OpdsPublication,
    sectionIndex: Int,
    hasMovableSections: Boolean,
    dragHandleModifier: Modifier,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pub_item_${sectionIndex}_$itemIndex"),
    ) {
         IconButton(onClick = {}, modifier = dragHandleModifier) {
             Icon(
                 imageVector = Icons.Filled.DragHandle,
                 contentDescription = stringResource(Res.string.move),
             )
         }
         ListItem(
             headlineContent = { Text(text = langMapString(publication.metadata.title)) },
             supportingContent = {
                 publication.metadata.description?.let { Text(text = it) }
             },
             modifier = Modifier.weight(1f),
         )
        Box {
            OpdsGroupItemMenuButton(
                sectionIndex = sectionIndex,
                itemIndex = itemIndex,
                hasMoveOption = hasMovableSections,
                onClickDelete = onClickDelete,
                onClickMove = onClickMove,
            )
        }
    }
}

@Composable
fun OpdsGroupItemMenuButton(
    sectionIndex: Int,
    itemIndex: Int,
    hasMoveOption: Boolean,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { menuExpanded = true },
        modifier = Modifier.testTag("item_menu_${sectionIndex}_$itemIndex"),
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(Res.string.move),
        )
    }
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
    ) {
        if (hasMoveOption) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.move)) },
                onClick = {
                    menuExpanded = false
                    onClickMove()
                },
                modifier = Modifier.testTag("item_move_${sectionIndex}_$itemIndex"),
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.delete)) },
            onClick = { menuExpanded = false; onClickDelete() },
            modifier = Modifier.testTag("item_delete_${sectionIndex}_$itemIndex"),
        )
    }
}

@Composable
fun MoveToSectionDialog(
    compatibleSections: List<MovingItemState.CompatibleSection>,
    allSections: List<OpdsGroup>,
    onClickSection: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.move_to_section)) },
        text = {
            compatibleSections.forEach { section ->
                val actualSection = allSections[section.sectionIndex]
                val sectionTitle = actualSection.metadata.title
                    .takeIf { it.isNotBlank() }
                    ?: stringResource(Res.string.section_title)
                val itemCount = (actualSection.navigation?.size ?: 0) +
                        (actualSection.publications?.size ?: 0)
                ListItem(
                    headlineContent = { Text(text = sectionTitle) },
                    supportingContent = {
                        Text(text = stringResource(Res.string.n_items, itemCount))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClickSection(section.sectionIndex) },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemTypeBottomSheet(
    onDismiss: () -> Unit,
    onClickBrowse: () -> Unit,
    onClickUseLink: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            text = stringResource(Res.string.choose_item_type),
            modifier = Modifier.defaultItemPadding(),
        )
        HorizontalDivider()
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(Res.string.browse),
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.browse)) },
            supportingContent = { Text(text = stringResource(Res.string.browse_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_item_type_browse")
                .clickable { onClickBrowse() },
        )
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = stringResource(Res.string.use_link),
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.use_link)) },
            supportingContent = { Text(text = stringResource(Res.string.use_link_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_item_type_use_link")
                .clickable { onClickUseLink() },
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}