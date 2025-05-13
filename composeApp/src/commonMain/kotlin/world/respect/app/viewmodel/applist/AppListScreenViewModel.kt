package world.respect.app.viewmodel.applist


import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.app.model.applist.AppListModel
import world.respect.app.viewmodel.RespectViewModel
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.select_app


data class AppListUiState(
    val appListData: List<AppListModel> = emptyList(),
)

class AppListScreenViewModel(
) : RespectViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.select_app),
                )
            }
        }
        loadAppListData()
    }

    //mock data for testing purpose
    private fun loadAppListData() {
        val appListData: List<AppListModel> = listOf(
            AppListModel("1", "App Name", "Category", "Age Range"),
            AppListModel("2", "App Name", "Category", "Age Range"),
            AppListModel("3", "App Name", "Category", "Age Range"),
            AppListModel("4", "App Name", "Category", "Age Range"),
            AppListModel("5", "App Name", "Category", "Age Range"),

            )

        _uiState.value = _uiState.value.copy(
            appListData = appListData
        )
    }
}
