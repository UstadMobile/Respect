package world.respect.app.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.filter_selected_count

data class RespectFilterDefinition(
    val id: String,
    val label: String,
    val options: List<String>,
)

@Composable
fun RespectFilterChip(
    filter: RespectFilterDefinition,
    selectedOptionIndices: List<Int>,
    onOptionCheckedChanged: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSheetOpen by rememberSaveable(filter.id) { mutableStateOf(false) }
    val selectedCount = filter.options.indices.count { it in selectedOptionIndices }

    FilterChip(
        selected = selectedCount > 0,
        onClick = { isSheetOpen = true },
        modifier = modifier,
        label = {
            Text(
                text = if (selectedCount > 0) {
                    stringResource(
                        Res.string.filter_selected_count,
                        filter.label,
                        selectedCount
                    )
                } else {
                    filter.label
                },
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
            )
        },
    )

    if (isSheetOpen) {
        RespectBottomSheet(
            title = filter.label,
            onDismissRequest = { isSheetOpen = false },
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(filter.options, key = { index, _ -> index }) { index, option ->
                    val isSelected = index in selectedOptionIndices
                    RespectBottomSheetOption(
                        headlineContent = { Text(option) },
                        trailingContent = {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = isSelected,
                                role = Role.Checkbox,
                                onValueChange = { onOptionCheckedChanged(index, it) },
                            ),
                    )
                }
            }
        }
    }
}
