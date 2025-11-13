package world.respect.shared.viewmodel.report.indictor.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.shared.paging.EmptyPagingSourceFactory
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.indicator
import world.respect.shared.generated.resources.indicators
import world.respect.shared.navigation.IndicatorDetail
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class IndicatorListUiState(
    val indicators:IPagingSourceFactory<Int, Indicator> = EmptyPagingSourceFactory(),
    val errorMessage: String? = null
)

class IndicatorListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val _uiState = MutableStateFlow(IndicatorListUiState())
    val uiState = _uiState.asStateFlow()
    override val scope: Scope = accountManager.requireSelectedAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.indicatorDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = IndicatorDataSource.GetListParams()
        )
    }
    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = Res.string.indicators.asUiText(),
                    fabState = FabUiState(
                        text = Res.string.indicator.asUiText(),
                        icon = FabUiState.FabIcon.ADD,
                        onClick = { this@IndicatorListViewModel.onClickAdd() },
                        visible = true
                    )
                )
            }
            viewModelScope.launch {
                try {
                    _uiState.update { prev ->
                        prev.copy(
                            indicators = pagingSourceHolder,
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "Failed to load indicators"
                        )
                    }
                }
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndictorEdit(null)
            )
        )
    }

    fun onIndicatorSelected(indicator: Indicator) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                IndicatorDetail(
                    indicator.indicatorId
                )
            )
        )
    }
}