package world.respect.app.view.report.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDateField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.lib.xapi.extensions.reportoptions.Comparisons
import world.respect.lib.xapi.extensions.reportoptions.FilterType
import world.respect.lib.xapi.extensions.reportoptions.FixedReportTimeRange
import world.respect.lib.xapi.extensions.reportoptions.RelativeRangeReportPeriod
import world.respect.lib.xapi.extensions.reportoptions.ReportFilter
import world.respect.lib.xapi.extensions.reportoptions.ReportOptions
import world.respect.lib.xapi.extensions.reportoptions.ReportPeriodOption
import world.respect.lib.xapi.extensions.reportoptions.ReportSeries
import world.respect.lib.xapi.extensions.reportoptions.ReportSeriesVisualType
import world.respect.lib.xapi.extensions.reportoptions.ReportTimeRangeUnit
import world.respect.lib.xapi.extensions.reportoptions.ReportXAxis
import world.respect.lib.xapi.extensions.reportoptions.Indicator
import world.respect.shared.ext.label
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_filter
import world.respect.shared.generated.resources.add_series
import world.respect.shared.generated.resources.chart_type
import world.respect.shared.generated.resources.filters
import world.respect.shared.generated.resources.from
import world.respect.shared.generated.resources.manage_indicators
import world.respect.shared.generated.resources.quantity
import world.respect.shared.generated.resources.remove
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
    viewModel: ReportEditViewModel
) {
    val uiState: ReportEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = ReportEditUiState(), context = Dispatchers.Main.immediate
    )
    ReportEditScreen(
        uiState = uiState,
        onReportOptionsChanged = viewModel::onEntityChanged,
        onSeriesChanged = viewModel::onSeriesChanged,
        onAddSeries = viewModel::onAddSeries,
        onAddFilter = viewModel::onAddFilter,
        onRemoveSeries = viewModel::onRemoveSeries,
        onRemoveFilter = viewModel::onRemoveFilter,
        manageIndicator = viewModel::onClickManageIndicator,
        onEditFilter = viewModel::onEditFilter
    )
}

@Composable
private fun ReportEditScreen(
    uiState: ReportEditUiState = ReportEditUiState(),
    onReportOptionsChanged: (ReportOptions) -> Unit = {},
    onAddSeries: () -> Unit = { },
    onAddFilter: (Int) -> Unit = { },
    onSeriesChanged: (Int, ReportSeries) -> Unit = { _, _ -> },
    onRemoveSeries: (Int) -> Unit = { },
    onRemoveFilter: (Int, Int) -> Unit = { _, _ -> },
    manageIndicator: () -> Unit = { },
    onEditFilter: (Int, Int, ReportFilter) -> Unit = { _, _, _ -> },
) {
    val reportOptions = uiState.reportOptions
    val firstIndicatorType = reportOptions.series.firstOrNull()?.reportSeriesYAxis?.type

    // Calculate disabled indicators (those with different types than the first series)
    val disabledIndicators =
        if (reportOptions.series.size > 1 && firstIndicatorType != null) {
            uiState.availableIndicators.filter { it.type != firstIndicatorType }
        } else {
            emptyList()
        }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = reportOptions.title,
                label = { Text(stringResource(Res.string.title) + "*") },
                onValueChange = { newTitle ->
                    onReportOptionsChanged(reportOptions.copy(title = newTitle))
                },
                supportingText = {
                    uiState.reportTitleError?.let {
                        Text(uiTextStringResource(it))
                    }
                },
                isError = uiState.hasErrors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
        }
        item {
            // Determine selected option based on TIME RANGE TYPE
            val selected = remember(reportOptions.period) {
                ReportPeriodOption.entries.find { option ->
                    when (val currentPeriod = reportOptions.period) {
                        is RelativeRangeReportPeriod -> {
                            val optionPeriod = option.period as? RelativeRangeReportPeriod
                            optionPeriod?.rangeUnit == currentPeriod.rangeUnit &&
                                    optionPeriod.rangeQuantity == currentPeriod.rangeQuantity
                        }

                        is FixedReportTimeRange -> {
                            option == ReportPeriodOption.CUSTOM_DATE_RANGE
                        }

                    }
                } ?: run {
                    when (reportOptions.period) {
                        is RelativeRangeReportPeriod -> ReportPeriodOption.CUSTOM_PERIOD
                        is FixedReportTimeRange -> ReportPeriodOption.CUSTOM_DATE_RANGE
                    }
                }
            }

            ExposedDropdownMenu(
                label = { Text(stringResource(Res.string.time_range) + "*") },
                options = ReportPeriodOption.entries,
                selectedValue = selected,
                onOptionSelected = { selectedOption ->
                    onReportOptionsChanged(reportOptions.copy(period = selectedOption.period))
                },
            )

            // Show CustomPeriodInputs only if selected is CUSTOM_PERIOD
            if (selected == ReportPeriodOption.CUSTOM_PERIOD) {
                // When selected is CUSTOM_PERIOD
                CustomPeriodInputs(
                    currentRange = reportOptions.period as RelativeRangeReportPeriod,
                    onCustomPeriodChanged = { qty, unit ->
                        onReportOptionsChanged(reportOptions.copy(period = RelativeRangeReportPeriod(unit, qty)))
                    },
                    quantityError = null
                )
            }

            // Show CustomDateRangeInputs only if selected is CUSTOM_DATE_RANGE
            if (selected == ReportPeriodOption.CUSTOM_DATE_RANGE) {
                CustomDateRangeInputs(
                    currentRange = reportOptions.period as FixedReportTimeRange,
                    onDateRangeChanged = { from, to ->
                        onReportOptionsChanged(reportOptions.copy(period = FixedReportTimeRange(from, to)))
                    }
                )
            }
        }

        item {
            ExposedDropdownMenu(
                selectedValue = reportOptions.xAxis,
                label = { Text(stringResource(Res.string.x_axis) + "*") },
                options = ReportXAxis.entries,
                onOptionSelected = { xAxis ->
                    onReportOptionsChanged(reportOptions.copy(xAxis = xAxis))
                }
            )
        }

        // Dynamically iterate over the series
        reportOptions.series.forEachIndexed { seriesIndex, seriesItem ->
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
                                onSeriesChanged(seriesIndex, updatedSeries)
                            }
                        )
                        if (!uiState.hasSingleSeries) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(Res.string.remove),
                                modifier = Modifier
                                    .clickable {
                                        onRemoveSeries(seriesIndex)
                                    }
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }

                    // Y Axis Dropdown
                    ExposedDropdownMenu(
                        selectedValue = seriesItem.reportSeriesYAxis,
                        label = { Text(stringResource(Res.string.y_axis) + "*") },
                        options = uiState.availableIndicators,
                        onOptionSelected = { selectedYAxis ->
                            val updatedSeries = seriesItem.copy(reportSeriesYAxis = selectedYAxis)
                            onSeriesChanged(seriesIndex, updatedSeries)
                        },
                        disabledOptions = disabledIndicators,
                        additionalMenuItems = {
                            DropdownMenuItem(
                                onClick = {
                                    manageIndicator()
                                },
                                text = {
                                    Text(
                                        text = stringResource(Res.string.manage_indicators),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    )

                    // Subgroup Dropdown
                    ExposedDropdownMenu(
                        label = { Text(stringResource(Res.string.subgroup_by)) },
                        options = if (reportOptions.xAxis.datePeriod != null) {
                            // X-axis is date - only show non-date options
                            ReportXAxis.entries.filter { it.datePeriod == null }
                        } else {
                            // X-axis is non-date - show date options plus other non-date options
                            ReportXAxis.entries.filter {
                                it.datePeriod != null ||  // Include all date options
                                        (it != reportOptions.xAxis)
                            }
                        },
                        selectedValue = seriesItem.reportSeriesSubGroup,
                        onOptionSelected = { selectedXAxis ->
                            val updatedSeries =
                                seriesItem.copy(reportSeriesSubGroup = selectedXAxis)
                            onSeriesChanged(seriesIndex, updatedSeries)
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
                            onSeriesChanged(seriesIndex, updatedSeries)
                        },
                    )
                    if (!seriesItem.reportSeriesFilters.isNullOrEmpty()) {
                        Text(stringResource(Res.string.filters))
                    }

                    seriesItem.reportSeriesFilters?.forEachIndexed { filterIndex, value ->
                        Row(
                            modifier = Modifier
                                .clickable {
                                    onEditFilter(seriesIndex, filterIndex, value)
                                }
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${value.reportFilterField?.let { stringResource(it.label) }} ${value.reportFilterCondition?.symbol} ${value.reportFilterValue}")
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(Res.string.remove),
                                modifier = Modifier
                                    .clickable {
                                        onRemoveFilter(seriesIndex, filterIndex)
                                    }
                            )
                        }
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        onAddFilter(seriesIndex)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.add_filter),
                    )
                }
            }
        }

        item {
            OutlinedButton(onClick = { onAddSeries() }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(Res.string.add_series),
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ExposedDropdownMenu(
    modifier: Modifier = Modifier,
    options: List<T>,
    selectedValue: T?,
    disabledOptions: List<T> = emptyList(),
    isError: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    onOptionSelected: (T) -> Unit,
    additionalMenuItems: @Composable (() -> Unit)? = null

) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedValue?.let {
                when (it) {
                    is ReportTimeRangeUnit -> stringResource(it.label)
                    is ReportPeriodOption -> stringResource(it.label)
                    is ReportXAxis -> stringResource(it.label)
                    is ReportSeriesVisualType -> stringResource(it.label)
                    is FilterType -> stringResource(it.label)
                    is Comparisons -> stringResource(it.label)
                    is Indicator -> (it.name)
                    else -> it.toString()
                }
            } ?: "",
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
                        val text = when (option) {
                            is ReportTimeRangeUnit -> stringResource(option.label)
                            is ReportPeriodOption -> stringResource(option.label)
                            is ReportXAxis -> stringResource(option.label)
                            is ReportSeriesVisualType -> stringResource(option.label)
                            is FilterType -> stringResource(option.label)
                            is Comparisons -> stringResource(option.label)
                            is Indicator -> (option.name)
                            else -> option.toString()
                        }
                        Text(
                            text,
                            color = if (isDisabled) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current
                        )
                    },
                    enabled = !isDisabled
                )
            }
            additionalMenuItems?.invoke()
        }
    }
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
            timeZoneId = TimeZone.currentSystemDefault().id,
            onValueChange = {
                onDateSelected(it)
            },
            supportingText = {}
        )
    }
}