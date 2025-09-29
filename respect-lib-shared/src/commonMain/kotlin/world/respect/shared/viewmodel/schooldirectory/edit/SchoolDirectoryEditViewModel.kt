package world.respect.shared.viewmodel.schooldirectory.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.ext.isReadyAndSettled
import world.respect.datalayer.opds.model.LangMapStringValue
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_directory
import world.respect.shared.generated.resources.error_link_message
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import kotlin.getValue
import kotlin.random.Random
import kotlin.time.Clock

data class SchoolDirectoryEditUIState(
    val linkUrl: String = "",
    val errorMessage: UiText? = null,
    val schoolDirectory: DataLoadState<SchoolDirectoryEntry> = DataLoadingState(),
) {
    val fieldsEnabled: Boolean
        get() = schoolDirectory.isReadyAndSettled()
}

class SchoolDirectoryEditViewModel(
    private val accountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val respectAppDataSource: RespectAppDataSource by inject()

    private val _uiState = MutableStateFlow(SchoolDirectoryEditUIState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.add_directory.asUiText(),
                hideBottomNavigation = true,
            )
        }
        viewModelScope.launch {

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
        viewModelScope.launch {
            val link = uiState.value.linkUrl.trim()

            try {
                val schoolBaseUrl = Url(link)

                val directoryEntry = SchoolDirectoryEntry(
                    name = LangMapStringValue(""),
                    self = schoolBaseUrl,
                    xapi = schoolBaseUrl.appendEndpointSegments("api/school/xapi"),
                    oneRoster = schoolBaseUrl.appendEndpointSegments("api/school/oneroster"),
                    respectExt = schoolBaseUrl.appendEndpointSegments("api/school/respect"),
                    schoolCode = Random.nextInt(10_000).toString().padStart(5, '0'),
                    directoryCode = null,
                    rpId = schoolBaseUrl.host,
                    lastModified = Clock.System.now(),
                    stored = Clock.System.now(),
                )

                respectAppDataSource.schoolDirectoryEntryDataSource.insertDirectoryEntry(
                    directoryEntry
                )
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        SchoolDirectoryList
                    )
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = Res.string.error_link_message.asUiText())
                }
            }
        }
    }
}
