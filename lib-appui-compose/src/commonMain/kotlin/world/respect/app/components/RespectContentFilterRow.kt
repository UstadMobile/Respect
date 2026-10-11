package world.respect.app.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.clear_all
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState

/**
 * Filter chips shown with the appbar search. They are shown only while search is expanded. The
 * selection is held by the ViewModel, which clears it when search is closed.
 *
 * @param searchState the appbar search state of the screen showing the filters.
 * @param filters the filters to show (e.g. as built using [contentFilterDefinitions]).
 * @param isOptionSelected whether the given option value is selected.
 * @param onToggleOption called when the user checks or unchecks an option.
 * @param clearAllEnabled true when at least one option is selected.
 * @param onClickClearAll called when the user clicks clear all.
 */
@Composable
fun <T> RespectContentFilterRow(
    searchState: AppBarSearchUiState,
    filters: List<RespectFilterDefinition<T>>,
    isOptionSelected: (T) -> Boolean,
    onToggleOption: (T) -> Unit,
    clearAllEnabled: Boolean,
    onClickClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!searchState.expanded || filters.isEmpty())
        return

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(FILTER_SPACING_DP.dp)
                .testTag(FILTER_CHIPS_TEST_TAG),
            horizontalArrangement = Arrangement.spacedBy(FILTER_SPACING_DP.dp),
        ) {
            filters.forEach { filter ->
                key(filter.id) {
                    RespectFilterChip(
                        filter = filter,
                        selectedOptionIndices = filter.options.indices.filter {
                            isOptionSelected(filter.options[it].value)
                        },
                        onOptionCheckedChanged = { index, _ ->
                            onToggleOption(filter.options[index].value)
                        },
                    )
                }
            }
        }
        TextButton(
            onClick = onClickClearAll,
            enabled = clearAllEnabled,
            modifier = Modifier.padding(end = CLEAR_BUTTON_END_PADDING_DP.dp),
        ) {
            Text(stringResource(Res.string.clear_all))
        }
    }
}

private const val FILTER_SPACING_DP = 16
private const val CLEAR_BUTTON_END_PADDING_DP = 8

const val FILTER_CHIPS_TEST_TAG = "content_filter_chips"
