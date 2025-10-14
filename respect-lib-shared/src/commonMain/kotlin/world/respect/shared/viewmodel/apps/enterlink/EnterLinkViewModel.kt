package world.respect.shared.viewmodel.apps.enterlink

import androidx.lifecycle.SavedStateHandle
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_link
import world.respect.shared.generated.resources.invalid_url
import world.respect.shared.resources.UiText
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText

data class EnterLinkUiState(
    val linkUrl: String = "",
    val errorMessage: UiText? = null,
)

class EnterLinkViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(EnterLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.enter_link.asUiText()
            )
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update {
            it.copy(
                linkUrl = link,
                errorMessage = null,
            )
        }
    }

    fun onClickNext() {
        launchWithLoadingIndicator {
            try {
                val linkUrl = Url(uiState.value.linkUrl)
                val appResult = appDataSource.compatibleAppsDataSource.getApp(
                    manifestUrl = linkUrl, loadParams = DataLoadParams()
                )

                if(appResult is DataReadyState) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            AppsDetail.create(linkUrl)
                        )
                    )
                } else {
                    throw (appResult as? DataErrorResult)?.error ?: IllegalStateException()
                }
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        errorMessage = StringResourceUiText(Res.string.invalid_url)
                    )
                }
            }
        }
    }

}
