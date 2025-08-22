package world.respect.shared.viewmodel.report.filteredit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.realm.model.report.FilterType
import world.respect.datalayer.realm.model.report.ReportConditionFilterOptions
import world.respect.datalayer.realm.model.report.ReportFilter
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.done
import world.respect.shared.generated.resources.edit_filters
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.util.ext.asUiText
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
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ReportFilterEditUiState())
    val uiState: StateFlow<ReportFilterEditUiState> = _uiState

    private val route: ReportEditFilter = savedStateHandle.toRoute()
    private val reportFilter: ReportFilter = route.getReportFilter()

    init {
        loadingState = LoadingUiState.INDETERMINATE
        viewModelScope.launch {
            _appUiState.update {
                AppUiState(
                    title = Res.string.edit_filters.asUiText(),
                    hideBottomNavigation = true,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = Res.string.done.asUiText(),
                        onClick = ::onClickSave
                    ),
                    userAccountIconVisible = false
                )
            }
        }
        _uiState.value = ReportFilterEditUiState(
            filters = reportFilter,
            filterConditionOptions = getConditionOptionsForField(reportFilter.reportFilterField)
        )
    }

    fun onClickSave() {
        val currentFilter = uiState.value.filters
        if (currentFilter != null && isValidFilter(currentFilter)) {
            sendResultAndPop(
                destKey = REPORT_FILTER_RESULT,
                destScreen = ReportEdit(route.reportUid),
                result = currentFilter
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

    companion object {
        const val REPORT_FILTER_RESULT = "report_filter_result"
    }
}