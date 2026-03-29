package world.respect.shared.viewmodel.apps.changehistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.ChangeHistoryEntryWithWhoDid
import world.respect.datalayer.school.model.ChangeHistoryTableEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.change_history
import world.respect.shared.navigation.ChangeHistory
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState


data class ChangeHistoryUiState(
    val guid: String = "",
    val changeHistoryEntryWithWhoDid: List<ChangeHistoryEntryWithWhoDid>? = null,
)

class ChangeHistoryViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    private val schoolDataSource: SchoolDataSource by inject()

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: ChangeHistory = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(
        ChangeHistoryUiState(
            guid = route.guid
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {

            val changeHistoryFlow =
                schoolDataSource.changeHistoryDataSource
                    .findByGuidAsFlow(route.guid)
                    .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

            changeHistoryFlow.collect { state ->

                val changeHistoryList = state.dataOrNull()?.map { entry ->
                            entry.copy(
                                changes = entry.changes.sortedByDescending { it.lastModified }
                            )
                }?.sortedByDescending { it.lastModified }

                val grouped =
                    changeHistoryList?.groupBy { it.whoGuid }

                val resultList =
                    grouped?.mapNotNull { (whoGuid, entries) ->

                        val person =
                            schoolDataSource.personDataSource.findByGuid(
                                loadParams = DataLoadParams(),
                                guid = whoGuid
                            ).dataOrNull()

                        person?.let {
                            ChangeHistoryEntryWithWhoDid(
                                person = it,
                                changeHistoryEntry = entries
                            )
                        }
                    }

                _uiState.update { previous ->
                    previous.copy(
                        changeHistoryEntryWithWhoDid = resultList
                    )
                }
            }
        }

        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.change_history.asUiText(),
                fabState = FabUiState()
            )
        }
    }

}