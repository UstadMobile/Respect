package world.respect.app.view.catalog.opdsfeeddetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectBottomSheet
import world.respect.app.components.RespectBottomSheetOption
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.arabic
import world.respect.shared.generated.resources.audio
import world.respect.shared.generated.resources.book
import world.respect.shared.generated.resources.clear_all
import world.respect.shared.generated.resources.english
import world.respect.shared.generated.resources.filter_selected_count
import world.respect.shared.generated.resources.grade
import world.respect.shared.generated.resources.grade_number
import world.respect.shared.generated.resources.hindi
import world.respect.shared.generated.resources.language
import world.respect.shared.generated.resources.mathematics
import world.respect.shared.generated.resources.science
import world.respect.shared.generated.resources.subject
import world.respect.shared.generated.resources.type
import world.respect.shared.generated.resources.video

data class OpdsFeedDefination(
    val id: String,
    val label: String,
    val options: List<String>,
)

@Composable
fun OpdsFeedDetailFilters(
    filters: List<OpdsFeedDefination> = defaultOpdsFeedFilters(),
    modifier: Modifier = Modifier,
) {
    var activeFilterId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedOptions by rememberSaveable { mutableStateOf(emptyList<String>()) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("feed_filter_dropdowns"),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            filters.forEach { filter ->
                val selectedCount = filter.options.indices.count {
                    "${filter.id}:$it" in selectedOptions
                }
                OutlinedButton(
                    onClick = { activeFilterId = filter.id },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("feed_filter_dropdown_${filter.id}"),
                ) {
                    Text(
                        text = if (selectedCount > 0) {
                            stringResource(
                                Res.string.filter_selected_count,
                                filter.label,
                                selectedCount,
                            )
                        } else {
                            filter.label
                        },
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                    )
                }
            }
        }
        TextButton(
            onClick = { selectedOptions = emptyList() },
            enabled = selectedOptions.isNotEmpty(),
            modifier = Modifier
                .padding(end = 8.dp)
        ) {
            Text(stringResource(Res.string.clear_all))
        }
    }

    filters.firstOrNull { it.id == activeFilterId }?.let { filter ->
        RespectBottomSheet(
            title = filter.label,
            onDismissRequest = { activeFilterId = null },
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(filter.options, key = { index, _ -> index }) { index, option ->
                    val optionId = "${filter.id}:$index"
                    val isSelected = optionId in selectedOptions
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
                                onValueChange = { checked ->
                                    selectedOptions = if (checked) {
                                        selectedOptions + optionId
                                    } else {
                                        selectedOptions - optionId
                                    }
                                },
                            ),
                    )
                }
            }
        }
    }
}

/** This is dummy data list will change it once get to know where the list is populated from
 * Sample options for the UI only; selections do not change the feed.*/

@Composable
private fun defaultOpdsFeedFilters(): List<OpdsFeedDefination> = listOf(
    OpdsFeedDefination(
        id = "language",
        label = stringResource(Res.string.language),
        options = listOf(
            stringResource(Res.string.english),
            stringResource(Res.string.hindi),
            stringResource(Res.string.arabic),
        ),
    ),
    OpdsFeedDefination(
        id = "grade",
        label = stringResource(Res.string.grade),
        options = (1..12).map { stringResource(Res.string.grade_number, it) },
    ),
    OpdsFeedDefination(
        id = "subject",
        label = stringResource(Res.string.subject),
        options = listOf(
            stringResource(Res.string.mathematics),
            stringResource(Res.string.science),
            stringResource(Res.string.english),
        ),
    ),
    OpdsFeedDefination(
        id = "type",
        label = stringResource(Res.string.type),
        options = listOf(
            stringResource(Res.string.book),
            stringResource(Res.string.audio),
            stringResource(Res.string.video),
        ),
    ),
)
