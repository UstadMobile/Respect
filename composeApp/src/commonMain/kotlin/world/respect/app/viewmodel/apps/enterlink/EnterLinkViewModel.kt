package world.respect.app.viewmodel.apps.enterlink

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.enter_link
import world.respect.app.app.AppsDetail
import world.respect.app.datasource.RespectAppDataSourceProvider
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.DataLoadParams
import world.respect.navigation.NavCommand

data class EnterLinkUiState(
    val reportData: List<String> = emptyList(),
    val isError: Boolean = false,
    var linkUrl: String=""
)

class EnterLinkViewModel(
    savedStateHandle: SavedStateHandle,
    dataSourceProvider: RespectAppDataSourceProvider,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(EnterLinkUiState())

    private val dataSource = dataSourceProvider.getDataSource(activeAccount)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(Res.string.enter_link)
                )
            }
            _uiState.update {
                //have to use the valid link here
                it.copy(
                    linkUrl = "",
                )
            }

        }
    }

    fun onButtonClick(link: String)  {
        try {
            viewModelScope.launch {
                dataSource.compatibleAppsDataSource.getApp(
                    manifestUrl = Url((link)), loadParams = DataLoadParams()
                )

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(AppsDetail(manifestUrl = link))
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isError = true,
                )
            }
        }
    }

}
