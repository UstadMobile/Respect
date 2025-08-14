package world.respect.shared.viewmodel.report.filteredit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.respect.RespectReportDataSource
import world.respect.shared.domain.report.model.FilterType
import world.respect.shared.domain.report.model.ReportConditionFilterOptions
import world.respect.shared.domain.report.model.ReportFilter
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.edit_filters
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState

data class ReportFilterEditUiState(
    val filters: ReportFilter? = ReportFilter(),
    val filterConditionOptions: ReportConditionFilterOptions? = null,
)

class ReportFilterEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectReportDataSource: RespectReportDataSource
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ReportFilterEditUiState())
    val uiState: StateFlow<ReportFilterEditUiState> = _uiState

    private val route: ReportEditFilter = savedStateHandle.toRoute()

    init {
        loadingState = LoadingUiState.INDETERMINATE
        viewModelScope.launch {
            _appUiState.update {
                AppUiState(
                    title = getString(resource = Res.string.edit_filters),
                    hideBottomNavigation = true,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = getString(resource = Res.string.done),
                        onClick = ::onClickSave
                    ),
                    userAccountIconVisible = false
                )
            }
        }
        val existingFilter = route.filter
        if (existingFilter != null) {
            handleExistingFilter(existingFilter)
        } else {
            handleNewFilter(savedStateHandle)
        }

    }

    private fun handleExistingFilter(existingFilter: ReportFilter) {
        try {
            _uiState.value = ReportFilterEditUiState(
                filters = existingFilter,
                filterConditionOptions = getConditionOptionsForField(existingFilter.reportFilterField)
            )
        } catch (e: Exception) {
            _uiState.value = ReportFilterEditUiState(
                filters = null,
                filterConditionOptions = null
            )
        }
    }

    private fun handleNewFilter(savedStateHandle: SavedStateHandle) {
        val tempFilterUid = route.filter?.reportFilterUid ?: 0
        val seriesUid = route.seriesId

        // Assign the temp UID to the new filter
        _uiState.value = if (tempFilterUid != null && seriesUid != null) {
            ReportFilterEditUiState(
                filters = ReportFilter(
                    reportFilterUid = tempFilterUid,
                    reportFilterSeriesUid = seriesUid
                )
            )
        } else {
            ReportFilterEditUiState(filters = null)
        }
    }

    fun onClickSave() {
        val currentFilter = uiState.value.filters
        if (currentFilter != null && isValidFilter(currentFilter)) {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ReportEdit.create(
                        reportUid = route.reportUid,
                        filter = currentFilter
                    )
                )
            )
        }
    }

    private fun isValidFilter(filter: ReportFilter): Boolean {
        return filter.reportFilterField != null &&
                filter.reportFilterCondition != null &&
                filter.reportFilterValue?.isNotBlank() == true
    }

    fun onEntityChanged(value: ReportFilter?) {
        val updatedFilter = value?.copy(
            reportFilterSeriesUid = _uiState.value.filters?.reportFilterSeriesUid ?: 0
        )

        _uiState.update { currentState ->
            currentState.copy(
                filters = updatedFilter,
                filterConditionOptions = getConditionOptionsForField(updatedFilter?.reportFilterField)
            )
        }
    }

    private fun getConditionOptionsForField(field: FilterType?): ReportConditionFilterOptions? {
        return when (field) {
            FilterType.PERSON_AGE -> ReportConditionFilterOptions.AgeConditionFilter()
            FilterType.PERSON_GENDER -> ReportConditionFilterOptions.GenderConditionFilter()
            else -> null
        }
    }
}