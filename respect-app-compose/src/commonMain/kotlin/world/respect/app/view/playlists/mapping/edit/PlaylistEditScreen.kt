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
import world.respect.shared.generated.resources.choose_section_type
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.learning_item_section
import world.respect.shared.generated.resources.learning_item_section_description
import world.respect.shared.generated.resources.move
import world.respect.shared.generated.resources.playlist_section
import world.respect.shared.generated.resources.playlist_section_description
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.section_title
import world.respect.shared.generated.resources.sections
import world.respect.shared.generated.resources.title
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.app.appstate.getTitle
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
    onClickMoveItem: (Int, Int, Int) -> Unit = { _, _, _ -> },
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
            label = { Text(stringResource(Res.string.title) + "*") },
            isError = uiState.titleError,
            supportingText = {
                Text(uiTextStringResource(Res.string.required.asUiText()))
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

        Text(
            text = stringResource(Res.string.sections),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.defaultItemPadding(),
        )

        // + Section button at top, left-aligned per prototype
        OutlinedButton(
            onClick = onClickAddSection,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .testTag("add_section_button"),
        ) {
            Text(text = stringResource(Res.string.add_section))
        }

        // Top layer ReorderableColumn for sections
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
                        onSectionTitleChanged = { t ->
                            onSectionTitleChanged(sectionIndex, t)
                        },
                        onClickDeleteSection = {
                            onClickDeleteSection(sectionIndex)
                        },
                        onClickAddItem = {
                            onClickAddItem(sectionIndex)
                        },
                        onClickAddPlaylist = {
                            onClickAddPlaylist(sectionIndex)
                        },
                        onClickDeleteItem = { itemIndex ->
                            onClickDeleteItem(sectionIndex, itemIndex)
                        },
                        onClickMoveItem = { itemIndex, targetSectionIndex ->
                            onClickMoveItem(sectionIndex, itemIndex, targetSectionIndex)
                        },
                        onItemsReordered = { items ->
                            onItemsReordered(sectionIndex, items)
                        },
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
    onClickMoveItem: (Int, Int) -> Unit,
    onItemsReordered: (List<Any>) -> Unit,
) {
    val isNavigationSection = section.navigation != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(
                onClick = {},
                modifier = dragHandleModifier,
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = null,
                )
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
                        val itemDragHandleModifier = Modifier
                            .draggableHandle()
                            .testTag("nav_drag_handle_${sectionIndex}_$itemIndex")

                        PlaylistNavItemRow(
                            itemIndex = itemIndex,
                            navLink = navLink,
                            sectionIndex = sectionIndex,
                            allSections = allSections,
                            dragHandleModifier = itemDragHandleModifier,
                            onClickDelete = { onClickDeleteItem(itemIndex) },
                            onClickMove = { targetIndex ->
                                onClickMoveItem(itemIndex, targetIndex)
                            },
                        )
                    }
                }
            }
            OutlinedButton(
                onClick = onClickAddPlaylist,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
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
                        val itemDragHandleModifier = Modifier
                            .draggableHandle()
                            .testTag("pub_drag_handle_${sectionIndex}_$itemIndex")

                        PlaylistPublicationItemRow(
                            itemIndex = itemIndex,
                            publication = publication,
                            sectionIndex = sectionIndex,
                            allSections = allSections,
                            dragHandleModifier = itemDragHandleModifier,
                            onClickDelete = { onClickDeleteItem(itemIndex) },
                            onClickMove = { targetIndex ->
                                onClickMoveItem(itemIndex, targetIndex)
                            },
                        )
                    }
                }
            }

            // + Item button left aligned per prototype
            OutlinedButton(
                onClick = onClickAddItem,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
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
    allSections: List<OpdsGroup>,
    dragHandleModifier: Modifier,
    onClickDelete: () -> Unit,
    onClickMove: (Int) -> Unit,
) {
    val movableSections = allSections.mapIndexedNotNull { index, section ->
        if (index != sectionIndex && section.navigation != null) index else null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nav_item_${sectionIndex}_$itemIndex"),
    ) {
        IconButton(
            onClick = {},
            modifier = dragHandleModifier,
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = null,
            )
        }
        ListItem(
            headlineContent = {
                Text(text = navLink.title ?: navLink.href)
            },
            modifier = Modifier.weight(1f),
        )
        // Box wraps the menu button so dropdown appears near it (right side)
        Box {
            ItemMenuButton(
                sectionIndex = sectionIndex,
                itemIndex = itemIndex,
                movableSections = movableSections,
                allSections = allSections,
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
    allSections: List<OpdsGroup>,
    dragHandleModifier: Modifier,
    onClickDelete: () -> Unit,
    onClickMove: (Int) -> Unit,
) {
    val movableSections = allSections.mapIndexedNotNull { index, section ->
        if (index != sectionIndex && section.publications != null) index else null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pub_item_${sectionIndex}_$itemIndex"),
    ) {
        IconButton(
            onClick = {},
            modifier = dragHandleModifier,
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = null,
            )
        }
        ListItem(
            headlineContent = {
                Text(text = publication.metadata.title.getTitle())
            },
            supportingContent = {
                publication.metadata.description?.let {
                    Text(text = it)
                }
            },
            modifier = Modifier.weight(1f),
        )
        // Box wraps the menu button so dropdown appears near it (right side)
        Box {
            ItemMenuButton(
                sectionIndex = sectionIndex,
                itemIndex = itemIndex,
                movableSections = movableSections,
                allSections = allSections,
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
    movableSections: List<Int>,
    allSections: List<OpdsGroup>,
    onClickDelete: () -> Unit,
    onClickMove: (Int) -> Unit,
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
        if (movableSections.isNotEmpty()) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.move)) },
                onClick = {
                    menuExpanded = false
                    onClickMove(movableSections.first())
                },
                modifier = Modifier.testTag("item_move_${sectionIndex}_$itemIndex"),
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.delete)) },
            onClick = {
                menuExpanded = false
                onClickDelete()
            },
            modifier = Modifier.testTag("item_delete_${sectionIndex}_$itemIndex"),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionTypeBottomSheet(
    onDismiss: () -> Unit,
    onClickSectionType: (PlaylistSectionType) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
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