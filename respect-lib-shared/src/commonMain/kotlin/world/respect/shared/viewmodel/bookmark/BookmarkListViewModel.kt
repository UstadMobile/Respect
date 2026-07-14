package world.respect.shared.viewmodel.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.ext.objectActivityNameOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.bookmark.RemoveBookmarkUseCase
import world.respect.shared.ext.tryOrShowSnackbarOnError
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.remove_bookmark
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher

data class BookmarkListUiState(
    val statements: List<XapiStatement> = emptyList(),
    val taskInfoFlow: (Url) -> Flow<DataLoadState<OpdsPublication>> = {
        flowOf(DataLoadingState())
    },
    val activeSortOrderOption: SortOrderOption = CommonSortOptions.DEFAULT,
    val sortOptions: List<SortOrderOption> = CommonSortOptions.ALL_OPTIONS,
)

class BookmarkListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    private val _uiState = MutableStateFlow(BookmarkListUiState())

    val uiState = _uiState.asStateFlow()

    private val activeSortOption = _uiState.map { it.activeSortOrderOption }.distinctUntilChanged()

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val removeBookmarkUseCase: RemoveBookmarkUseCase by inject()

    init {
        _appUiState.update {
            it.copy(title = Res.string.home.asUiText())
        }

        _uiState.update {
            it.copy(
                taskInfoFlow = ::taskInfoFlowFor
            )
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    agent = accountManager.selectedAccountAndPersonFlow.firstOrNull()?.xapiAgent,
                    verb = XapiVerb.ID_BOOKMARKED,
                    activity = OpenEelXapiConstants.CATEGORY_BOOKMARK_RECIPE,
                    relatedActivities = true,
                ),
                dataLoadParams = DataLoadParams(),
            ).combine(activeSortOption) { statements, sortOrderOption ->
                Pair(statements, sortOrderOption)
            }.collect { (statements, sortOrderOption) ->
                val stmtList = statements.dataOrNull()?.statements ?: emptyList()
                _uiState.update { prev ->
                    prev.copy(
                        statements = when(sortOrderOption.flag) {
                            CommonSortOptions.FLAG_TIME_ASC -> stmtList.sortedBy { it.timestamp }
                            CommonSortOptions.FLAG_TIME_DESC -> stmtList.sortedByDescending { it.timestamp }
                            CommonSortOptions.FLAG_TITLE_ASC -> stmtList.sortedBy {
                                it.objectActivityNameOrNull()?.entries?.firstOrNull()?.value
                            }
                            CommonSortOptions.FLAG_TITLE_DESC -> stmtList.sortedByDescending {
                                it.objectActivityNameOrNull()?.entries?.firstOrNull()?.value
                            }
                            else -> stmtList
                        }
                    )
                }
            }
        }
    }

    fun onSortOrderChanged(sortOrderOption: SortOrderOption) {
        _uiState.update {
            it.copy(
                activeSortOrderOption = sortOrderOption,
            )
        }
    }

    fun taskInfoFlowFor(url: Url): Flow<DataLoadState<OpdsPublication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        )
    }

    fun onClickRemoveBookmark(statement: XapiStatement) {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError(
                logMessage = "BookmarkListViewModel: error removing bookmark"
            ) {
                removeBookmarkUseCase(statements = listOf(statement),)

                _uiState.update {
                    it.copy(
                        statements = it.statements.filterNot { s -> s.id == statement.id }
                    )
                }

                snackBarDispatcher.showSnackBar(
                    Snack(
                        message = Res.string.remove_bookmark.asUiText(),
                    )
                )
            }
        }
    }

    fun onClickBookmark(statement: XapiStatement) {
        val activityId = (statement.`object` as? XapiActivity)?.id
        if (activityId == null) {
            Napier.w("Cannot navigate to bookmark: statement object is not an Activity")
            snackBarDispatcher.showSnackBar(
                Snack(message = Res.string.something_went_wrong.asUiText())
            )
            return
        }

        _navCommandFlow.tryEmit(
            value = NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = Url(activityId)
                )
            )
        )
    }
}
