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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.sample.defaultContentFilters
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.clear_all
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState

/**
 * Filter chips shown with the appbar search. They are shown only while search is expanded, and
 * selections are kept while hidden so that they are not lost if the user closes and reopens search.
 *
 * @param searchState the appbar search state of the screen showing the filters.
 */
@Composable
fun RespectContentFilterRow(
    searchState: AppBarSearchUiState,
    modifier: Modifier = Modifier,
) {
    val filters = defaultContentFilters()
    var selectedOptions by rememberSaveable { mutableStateOf(emptyList<String>()) }

    if (!searchState.expanded)
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
                            "${filter.id}:$it" in selectedOptions
                        },
                        onOptionCheckedChanged = { index, checked ->
                            val optionId = "${filter.id}:$index"
                            selectedOptions = if (checked) {
                                (selectedOptions + optionId).distinct()
                            } else {
                                selectedOptions - optionId
                            }
                        },
                    )
                }
            }
        }
        TextButton(
            onClick = { selectedOptions = emptyList() },
            enabled = selectedOptions.isNotEmpty(),
            modifier = Modifier.padding(end = CLEAR_BUTTON_END_PADDING_DP.dp),
        ) {
            Text(stringResource(Res.string.clear_all))
        }
    }
}

private const val FILTER_SPACING_DP = 16
private const val CLEAR_BUTTON_END_PADDING_DP = 8

const val FILTER_CHIPS_TEST_TAG = "content_filter_chips"
