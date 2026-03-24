package world.respect.app.view.playlists.mapping.edit

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
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
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
import sh.calvin.reorderable.ReorderableItem
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_item
import world.respect.shared.generated.resources.add_new_playlist
import world.respect.shared.generated.resources.add_section
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.choose_section_type
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.learning_item_section
import world.respect.shared.generated.resources.learning_item_section_description
import world.respect.shared.generated.resources.move
import world.respect.shared.generated.resources.move_to_section
import world.respect.shared.generated.resources.n_items
import world.respect.shared.generated.resources.playlist_section
import world.respect.shared.generated.resources.playlist_section_description
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.section_title
import world.respect.shared.generated.resources.sections
import world.respect.shared.generated.resources.title
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.playlists.mapping.edit.MovingItemState
import world.respect.shared.viewmodel.playlists.mapping.edit.PlaylistEditUiState
import world.respect.shared.viewmodel.playlists.mapping.edit.PlaylistEditViewModel
import world.respect.shared.viewmodel.playlists.mapping.edit.PlaylistSectionType

@Composable
fun PlaylistEditScreenForViewModel(
    viewModel: PlaylistEditViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    PlaylistEditScreen(
        uiState = uiState,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onSectionTitleChanged = viewModel::onSectionTitleChanged,
        onClickAddSection = viewModel::onClickAddSection,
        onDismissSectionTypeBottomSheet = viewModel::onDismissSectionTypeDialog,
        onClickSectionType = viewModel::onClickSectionType,
        onClickDeleteSection = viewModel::onClickDeleteSection,
        onSectionsReordered = viewModel::onSectionsReordered,
        onClickAddItem = viewModel::onClickAddItem,
        onClickAddPlaylist = viewModel::onClickAddPlaylist,
        onClickDeleteItem = viewModel::onClickDeleteItem,
        onClickMoveItem = viewModel::onClickMoveItem,
        onClickMoveItemToSection = viewModel::onClickMoveItemToSection,
        onDismissMoveDialog = viewModel::onDismissMoveDialog,
        onItemsReordered = viewModel::onItemsReordered,
    )
}

@Composable
fun PlaylistEditScreen(
    uiState: PlaylistEditUiState = PlaylistEditUiState(),
    onTitleChanged: (String) -> Unit = {},
    onDescriptionChanged: (String) -> Unit = {},
    onSectionTitleChanged: (Int, String) -> Unit = { _, _ -> },
    onClickAddSection: () -> Unit = {},
    onDismissSectionTypeBottomSheet: () -> Unit = {},
    onClickSectionType: (PlaylistSectionType) -> Unit = {},
    onClickDeleteSection: (Int) -> Unit = {},
    onSectionsReordered: (List<OpdsGroup>) -> Unit = {},
    onClickAddItem: (Int) -> Unit = {},
    onClickAddPlaylist: (Int) -> Unit = {},
    onClickDeleteItem: (Int, Int) -> Unit = { _, _ -> },
    onClickMoveItem: (Int, Int) -> Unit = { _, _ -> },
    onClickMoveItemToSection: (Int) -> Unit = {},
    onDismissMoveDialog: () -> Unit = {},
    onItemsReordered: (Int, List<Any>) -> Unit = { _, _ -> },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChanged,
            label = { Text(stringResource(Res.string.title)) },
            isError = uiState.titleError,
            supportingText = {
                if (uiState.titleError) {
                    Text(uiTextStringResource(Res.string.required.asUiText()))
                }
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
            onClick = onClickAddSection,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("add_section_button"),
        ) {
            Text(text = stringResource(Res.string.add_section))
        }

        ReorderableColumn(
            list = uiState.sections,
            onSettle = { fromIndex, toIndex ->
                val sections = uiState.sections.toMutableList()
                val item = sections.removeAt(fromIndex)
                sections.add(toIndex, item)
                onSectionsReordered(sections)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { sectionIndex, section, _ ->
            key(sectionIndex) {
                ReorderableItem {
                    val sectionDragHandleModifier = Modifier
                        .draggableHandle()
                        .testTag("section_drag_handle_$sectionIndex")

                    PlaylistSectionEditItem(
                        sectionIndex = sectionIndex,
                        section = section,
                        allSections = uiState.sections,
                        dragHandleModifier = sectionDragHandleModifier,
                        onSectionTitleChanged = { t -> onSectionTitleChanged(sectionIndex, t) },
                        onClickDeleteSection = { onClickDeleteSection(sectionIndex) },
                        onClickAddItem = { onClickAddItem(sectionIndex) },
                        onClickAddPlaylist = { onClickAddPlaylist(sectionIndex) },
                        onClickDeleteItem = { itemIndex -> onClickDeleteItem(sectionIndex, itemIndex) },
                        onClickMoveItem = { itemIndex -> onClickMoveItem(sectionIndex, itemIndex) },
                        onItemsReordered = { items -> onItemsReordered(sectionIndex, items) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (uiState.isSectionTypeDialogVisible) {
        SectionTypeBottomSheet(
            onDismiss = onDismissSectionTypeBottomSheet,
            onClickSectionType = onClickSectionType,
        )
    }

    uiState.movingItem?.let { movingItem ->
        MoveToSectionDialog(
            sections = movingItem.compatibleSections,
            onClickSection = onClickMoveItemToSection,
            onDismiss = onDismissMoveDialog,
        )
    }
}

@Composable
private fun PlaylistSectionEditItem(
    sectionIndex: Int,
    section: OpdsGroup,
    allSections: List<OpdsGroup>,
    dragHandleModifier: Modifier,
    onSectionTitleChanged: (String) -> Unit,
    onClickDeleteSection: () -> Unit,
    onClickAddItem: () -> Unit,
    onClickAddPlaylist: () -> Unit,
    onClickDeleteItem: (Int) -> Unit,
    onClickMoveItem: (Int) -> Unit,
    onItemsReordered: (List<Any>) -> Unit,
) {
    val isNavigationSection = section.navigation != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            IconButton(
                onClick = {},
                modifier = dragHandleModifier,
            ) {
                Icon(imageVector = Icons.Filled.DragHandle, contentDescription = null)
            }

            OutlinedTextField(
                value = section.metadata.title,
                onValueChange = onSectionTitleChanged,
                label = { Text(stringResource(Res.string.section_title)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("section_title_field_$sectionIndex"),
                singleLine = true,
            )

            IconButton(
                onClick = onClickDeleteSection,
                modifier = Modifier.testTag("delete_section_$sectionIndex"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(Res.string.delete),
                )
            }
        }

        if (isNavigationSection) {
            val navItems = section.navigation ?: emptyList()

            ReorderableColumn(
                list = navItems,
                onSettle = { from, to ->
                    val items = navItems.toMutableList()
                    val item = items.removeAt(from)
                    items.add(to, item)
                    onItemsReordered(items)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { itemIndex, navLink, _ ->
                key(itemIndex) {
                    ReorderableItem {
                        PlaylistNavItemRow(
                            itemIndex = itemIndex,
                            navLink = navLink,
                            sectionIndex = sectionIndex,
                            hasMovableSections = allSections.any { s ->
                                s != section && s.navigation != null
                            },
                            dragHandleModifier = Modifier
                                .draggableHandle()
                                .testTag("nav_drag_handle_${sectionIndex}_$itemIndex"),
                            onClickDelete = { onClickDeleteItem(itemIndex) },
                            onClickMove = { onClickMoveItem(itemIndex) },
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onClickAddPlaylist,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .testTag("add_playlist_button_$sectionIndex"),
            ) {
                Text(text = stringResource(Res.string.add_new_playlist))
            }
        } else {
            val pubItems = section.publications ?: emptyList()

            ReorderableColumn(
                list = pubItems,
                onSettle = { from, to ->
                    val items = pubItems.toMutableList()
                    val item = items.removeAt(from)
                    items.add(to, item)
                    onItemsReordered(items)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { itemIndex, publication, _ ->
                key(itemIndex) {
                    ReorderableItem {
                        PlaylistPublicationItemRow(
                            itemIndex = itemIndex,
                            publication = publication,
                            sectionIndex = sectionIndex,
                            hasMovableSections = allSections.any { s ->
                                s != section && s.publications != null
                            },
                            dragHandleModifier = Modifier
                                .draggableHandle()
                                .testTag("pub_drag_handle_${sectionIndex}_$itemIndex"),
                            onClickDelete = { onClickDeleteItem(itemIndex) },
                            onClickMove = { onClickMoveItem(itemIndex) },
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onClickAddItem,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .testTag("add_item_button_$sectionIndex"),
            ) {
                Text(text = stringResource(Res.string.add_item))
            }
        }
    }
}

@Composable
private fun PlaylistNavItemRow(
    itemIndex: Int,
    navLink: ReadiumLink,
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
            .testTag("nav_item_${sectionIndex}_$itemIndex"),
    ) {
        IconButton(onClick = {}, modifier = dragHandleModifier) {
            Icon(imageVector = Icons.Filled.DragHandle, contentDescription = null)
        }
        ListItem(
            headlineContent = {
                Text(
                    text = navLink.title?.takeIf { it.isNotBlank() } ?: navLink.href,
                )
            },
            modifier = Modifier.weight(1f),
        )
        Box {
            ItemMenuButton(
                sectionIndex = sectionIndex,
                itemIndex = itemIndex,
                hasMovableSections = hasMovableSections,
                onClickDelete = onClickDelete,
                onClickMove = onClickMove,
            )
        }
    }
}

@Composable
private fun PlaylistPublicationItemRow(
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
            Icon(imageVector = Icons.Filled.DragHandle, contentDescription = null)
        }
        ListItem(
            headlineContent = { Text(text = publication.metadata.title.getTitle()) },
            supportingContent = {
                publication.metadata.description?.let { Text(text = it) }
            },
            modifier = Modifier.weight(1f),
        )
        Box {
            ItemMenuButton(
                sectionIndex = sectionIndex,
                itemIndex = itemIndex,
                hasMovableSections = hasMovableSections,
                onClickDelete = onClickDelete,
                onClickMove = onClickMove,
            )
        }
    }
}

@Composable
private fun ItemMenuButton(
    sectionIndex: Int,
    itemIndex: Int,
    hasMovableSections: Boolean,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { menuExpanded = true },
        modifier = Modifier.testTag("item_menu_${sectionIndex}_$itemIndex"),
    ) {
        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(Res.string.move))
    }
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
    ) {
        if (hasMovableSections) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.move)) },
                onClick = { menuExpanded = false; onClickMove() },
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
private fun MoveToSectionDialog(
    sections: List<MovingItemState.CompatibleSection>,
    onClickSection: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.move_to_section)) },
        text = {
            Column {
                sections.forEach { section ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = section.title.takeIf { it.isNotBlank() }
                                    ?: stringResource(Res.string.section_title),
                            )
                        },
                        supportingContent = {
                            Text(text = stringResource(Res.string.n_items, section.itemCount))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickSection(section.sectionIndex) },
                    )
                }
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
private fun SectionTypeBottomSheet(
    onDismiss: () -> Unit,
    onClickSectionType: (PlaylistSectionType) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            text = stringResource(Res.string.choose_section_type),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.defaultItemPadding(),
        )
        HorizontalDivider()
        SectionTypeItem(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = stringResource(Res.string.playlist_section),
                )
            },
            title = stringResource(Res.string.playlist_section),
            description = stringResource(Res.string.playlist_section_description),
            onClick = { onClickSectionType(PlaylistSectionType.NAVIGATION) },
            testTag = "section_type_playlist",
        )
        SectionTypeItem(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Book,
                    contentDescription = stringResource(Res.string.learning_item_section),
                )
            },
            title = stringResource(Res.string.learning_item_section),
            description = stringResource(Res.string.learning_item_section_description),
            onClick = { onClickSectionType(PlaylistSectionType.PUBLICATION) },
            testTag = "section_type_learning_item",
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTypeItem(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
    testTag: String,
) {
    ListItem(
        leadingContent = icon,
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = description) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable { onClick() },
    )
}