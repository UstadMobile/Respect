package world.respect.app.view.report.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDateField
import world.respect.app.util.ext.defaultItemPadding
import world.respect.shared.domain.report.model.FixedReportTimeRange
import world.respect.shared.domain.report.model.OptionWithLabelStringResource
import world.respect.shared.domain.report.model.RelativeRangeReportPeriod
import world.respect.shared.domain.report.model.ReportOptions
import world.respect.shared.domain.report.model.ReportPeriodOption
import world.respect.shared.domain.report.model.ReportSeries
import world.respect.shared.domain.report.model.ReportSeriesVisualType
import world.respect.shared.domain.report.model.ReportSeriesYAxis
import world.respect.shared.domain.report.model.ReportTimeRangeUnit
import world.respect.shared.domain.report.model.ReportXAxis
import world.respect.shared.domain.report.model.YAxisTypes
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_series
import world.respect.shared.generated.resources.chart_type
import world.respect.shared.generated.resources.from
import world.respect.shared.generated.resources.quantity
import world.respect.shared.generated.resources.remove
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.series_title
import world.respect.shared.generated.resources.subgroup_by
import world.respect.shared.generated.resources.time_range
import world.respect.shared.generated.resources.title
import world.respect.shared.generated.resources.to_
import world.respect.shared.generated.resources.unit
import world.respect.shared.generated.resources.x_axis
import world.respect.shared.generated.resources.y_axis
import world.respect.shared.viewmodel.report.edit.ReportEditUiState
import world.respect.shared.viewmodel.report.edit.ReportEditViewModel

@Composable
fun ReportEditScreen(
    navController: NavHostController,
    viewModel: ReportEditViewModel
) {
    val uiState: ReportEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = ReportEditUiState(), context = Dispatchers.Main.immediate
    )
    ReportEditScreen(
        uiState = uiState,
        onReportChanged = viewModel::onEntityChanged,
        onSeriesChanged = viewModel::onSeriesChanged,
        onAddSeries = viewModel::onAddSeries,
        onRemoveSeries = viewModel::onRemoveSeries,
    )
}

@Composable
private fun ReportEditScreen(
    uiState: ReportEditUiState = ReportEditUiState(),
    onReportChanged: (ReportOptions) -> Unit = {},
    onAddSeries: () -> Unit = { },
    onSeriesChanged: (ReportSeries) -> Unit = {},
    onRemoveSeries: (Int) -> Unit = { },
) {
    val requiredYAxisType: YAxisTypes? = uiState.reportOptions.series
        .mapNotNull { it.reportSeriesYAxis?.type }
        .distinct()
        .singleOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.reportOptions.title,
                label = { Text(stringResource(Res.string.title) + "*") },
                singleLine = true,
                onValueChange = { newTitle ->
                    val updatedOptions = uiState.reportOptions.copy(title = newTitle)
                    onReportChanged(updatedOptions)
                },
                isError = uiState.submitted && uiState.reportTitleError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                supportingText = {
                    Text(uiState.reportTitleError ?: stringResource(Res.string.required))
                }
            )

        }
        item {
            // Determine selected option based on TIME RANGE TYPE
            val selected = remember(uiState.reportOptions.period) {
                ReportPeriodOption.entries.find { option ->
                    when (val currentPeriod = uiState.reportOptions.period) {
                        is RelativeRangeReportPeriod -> {
                            val optionPeriod = option.period as? RelativeRangeReportPeriod
                            optionPeriod?.rangeUnit == currentPeriod.rangeUnit &&
                                    optionPeriod?.rangeQuantity == currentPeriod.rangeQuantity
                        }

                        is FixedReportTimeRange -> {
                            option == ReportPeriodOption.CUSTOM_DATE_RANGE
                        }

                        else -> false
                    }
                } ?: run {
                    when (uiState.reportOptions.period) {
                        is RelativeRangeReportPeriod -> ReportPeriodOption.CUSTOM_PERIOD
                        is FixedReportTimeRange -> ReportPeriodOption.CUSTOM_DATE_RANGE
                        else -> null
                    }
                }
            }

            ExposedDropdownMenu(
                label = { Text(stringResource(Res.string.time_range) + "*") },
                options = ReportPeriodOption.entries,
                selectedValue = selected,
                onOptionSelected = { selectedOption ->
                    handleTimeRangeSelection(
                        selectedOption,
                        uiState.reportOptions,
                        onReportChanged
                    )
                },
                isError = uiState.submitted && uiState.timeRangeError != null && selected == null,
                supportingText = {
                    if (selected == null) {
                        Text(uiState.timeRangeError ?: stringResource(Res.string.required))
                    } else {
                        Text("")
                    }
                }
            )

            // Show CustomPeriodInputs only if selected is CUSTOM_PERIOD
            if (selected == ReportPeriodOption.CUSTOM_PERIOD) {
                // When selected is CUSTOM_PERIOD
                CustomPeriodInputs(
                    currentRange = uiState.reportOptions.period as RelativeRangeReportPeriod,
                    onCustomPeriodChanged = { qty, unit ->
                        val newRange = RelativeRangeReportPeriod(unit, qty)
                        val updatedOptions = uiState.reportOptions.copy(period = newRange)
                        onReportChanged(updatedOptions)
                    },
                    quantityError = uiState.quantityError
                )
            }

            // Show CustomDateRangeInputs only if selected is CUSTOM_DATE_RANGE
            if (selected == ReportPeriodOption.CUSTOM_DATE_RANGE) {
                CustomDateRangeInputs(
                    currentRange = uiState.reportOptions.period as FixedReportTimeRange,
                    onDateRangeChanged = { from, to ->
                        val newRange = FixedReportTimeRange(from, to)
                        val updatedOptions = uiState.reportOptions.copy(period = newRange)
                        onReportChanged(updatedOptions)
                    }
                )
            }
        }

        item {
            ExposedDropdownMenu(
                selectedValue = uiState.reportOptions.xAxis,
                label = { Text(stringResource(Res.string.x_axis) + "*") },
                options = ReportXAxis.entries,
                onOptionSelected = {
                    val updatedOptions = uiState.reportOptions.copy(xAxis = it)
                    onReportChanged(updatedOptions)
                },
                isError = uiState.submitted && uiState.xAxisError != null,
                supportingText = {
                    Text(uiState.xAxisError ?: stringResource(Res.string.required))
                }
            )
        }

        // Dynamically iterate over the series
        uiState.reportOptions.series.forEach { seriesItem ->
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Series Title Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = seriesItem.reportSeriesTitle,
                            label = {
                                Text(
                                    stringResource(Res.string.series_title) + "*",
                                )
                            },
                            singleLine = true,
                            onValueChange = { newTitle ->
                                val updatedSeries = seriesItem.copy(reportSeriesTitle = newTitle)
                                onSeriesChanged(updatedSeries)
                            },
                            isError = uiState.submitted && uiState.seriesTitleErrors[seriesItem.reportSeriesUid] != null,
                            supportingText = {
                                Text(
                                    uiState.seriesTitleErrors[seriesItem.reportSeriesUid]
                                        ?: stringResource(Res.string.required)
                                )
                            },
                        )
                        if (!uiState.hasSingleSeries) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(Res.string.remove),
                                modifier = Modifier
                                    .clickable {
                                        onRemoveSeries(seriesItem.reportSeriesUid)
                                    }
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }

                    // Y Axis Dropdown
                    ExposedDropdownMenu(
                        label = { Text(stringResource(Res.string.y_axis) + "*") },
                        options = ReportSeriesYAxis.entries,
                        selectedValue = seriesItem.reportSeriesYAxis,
                        onOptionSelected = { selectedYAxis ->
                            val updatedSeries = seriesItem.copy(reportSeriesYAxis = selectedYAxis)
                            onSeriesChanged(updatedSeries)
                        },
                        isError = uiState.submitted && uiState.yAxisErrors[seriesItem.reportSeriesUid] != null,
                        supportingText = {
                            Text(
                                uiState.yAxisErrors[seriesItem.reportSeriesUid]
                                    ?: stringResource(Res.string.required)
                            )
                        },
                        disabledOptions = if (uiState.reportOptions.series.size > 1) {
                            requiredYAxisType?.let { requiredType ->
                                ReportSeriesYAxis.entries.filter { it.type != requiredType }
                            } ?: emptyList()
                        } else {
                            emptyList()
                        }
                    )

                    // Subgroup Dropdown
                    ExposedDropdownMenu(
                        label = { Text(stringResource(Res.string.subgroup_by)) },
                        options = if (uiState.reportOptions.xAxis?.datePeriod != null) {
                            // X-axis is date - only show non-date options
                            ReportXAxis.entries.filter { it.datePeriod == null }
                        } else {
                            // X-axis is non-date - show date options plus other non-date options
                            ReportXAxis.entries.filter {
                                it.datePeriod != null ||  // Include all date options
                                        (it.datePeriod == null && it != uiState.reportOptions.xAxis)
                            }
                        },
                        selectedValue = seriesItem.reportSeriesSubGroup,
                        onOptionSelected = { selectedXAxis ->
                            val updatedSeries =
                                seriesItem.copy(reportSeriesSubGroup = selectedXAxis)
                            onSeriesChanged(updatedSeries)
                        }
                    )


                    // Chart Type Dropdown
                    ExposedDropdownMenu(
                        label = { Text(stringResource(Res.string.chart_type) + "*") },
                        options = ReportSeriesVisualType.entries,
                        selectedValue = seriesItem.reportSeriesVisualType,
                        onOptionSelected = { selectedVisualType ->
                            val updatedSeries =
                                seriesItem.copy(reportSeriesVisualType = selectedVisualType)
                            onSeriesChanged(updatedSeries)
                        },
                        supportingText = {
                            Text(
                                uiState.chartTypeError[seriesItem.reportSeriesUid]
                                    ?: stringResource(Res.string.required)
                            )
                        },
                        isError = uiState.submitted && uiState.chartTypeError[seriesItem.reportSeriesUid] != null,
                    )

                }
            }
        }

        item {
            Button(onClick = { onAddSeries() }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(Res.string.add_series),
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : OptionWithLabelStringResource> ExposedDropdownMenu(
    options: List<T>,
    selectedValue: T?,
    disabledOptions: List<T> = emptyList(),
    modifier: Modifier = Modifier.fillMaxWidth(),
    isError: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    onOptionSelected: (T) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue?.let { stringResource(it.label) } ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            supportingText = supportingText,
            isError = isError,
            label = label,
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
        ) {
            options.forEach { option ->
                val isDisabled = option in disabledOptions
                DropdownMenuItem(
                    onClick = {
                        if (!isDisabled) {
                            onOptionSelected(option)
                            isExpanded = false
                        }
                    },
                    text = {
                        Text(
                            stringResource(option.label),
                            color = if (isDisabled) Color.Gray else LocalContentColor.current
                        )
                    },
                    enabled = !isDisabled
                )
            }
        }
    }
}

fun handleTimeRangeSelection(
    selectedOption: ReportPeriodOption,
    currentOptions: ReportOptions,
    onReportChanged: (ReportOptions) -> Unit
) {
    val newOptions = currentOptions.copy(
        period = selectedOption.period
    )

    onReportChanged(newOptions)
}

@Composable
fun CustomPeriodInputs(
    currentRange: RelativeRangeReportPeriod,
    onCustomPeriodChanged: (Int, ReportTimeRangeUnit) -> Unit,
    quantityError: String?
) {
    var quantity by remember { mutableStateOf(currentRange.rangeQuantity.toString()) }
    var selectedUnit by remember { mutableStateOf(currentRange.rangeUnit) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1.2f),
            value = quantity,
            onValueChange = {
                quantity = it
                it.toIntOrNull()?.let { qty ->
                    onCustomPeriodChanged(qty, selectedUnit)
                }
            },
            label = { Text(stringResource(Res.string.quantity)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = quantityError != null,
            supportingText = { quantityError?.let { Text(it) } },
        )


        ExposedDropdownMenu(
            modifier = Modifier.weight(0.8f),
            label = { Text(stringResource(Res.string.unit)) },
            options = ReportTimeRangeUnit.entries,
            selectedValue = selectedUnit,
            onOptionSelected = { unit ->
                selectedUnit = unit
                quantity.toIntOrNull()?.let { qty ->
                    onCustomPeriodChanged(qty, unit)
                }
            }
        )
    }
}

@Composable
fun CustomDateRangeInputs(
    currentRange: FixedReportTimeRange,
    onDateRangeChanged: (Long, Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DatePickerButton(
            label = stringResource(Res.string.from),
            timestamp = currentRange.fromDateMillis,
            onDateSelected = { newFrom ->
                onDateRangeChanged(newFrom, currentRange.toDateMillis)
            },
            modifier = Modifier.weight(1f)
        )

        DatePickerButton(
            label = stringResource(Res.string.to_),
            timestamp = currentRange.toDateMillis,
            onDateSelected = { newTo ->
                onDateRangeChanged(currentRange.fromDateMillis, newTo)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DatePickerButton(
    label: String,
    timestamp: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        RespectDateField(
            modifier = Modifier.fillMaxWidth(),
            value = timestamp,
            label = { Text(label) },
            timeZoneId = "",
            onValueChange = {
                onDateSelected(it)
            },
            supportingText = {}
        )
    }
}
