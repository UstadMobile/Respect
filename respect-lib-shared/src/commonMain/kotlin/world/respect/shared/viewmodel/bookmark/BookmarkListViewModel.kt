package world.respect.shared.viewmodel.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.viewmodel.RespectViewModel
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.remove_bookmark
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.getValue

data class BookmarkListUiState(
    val statements: List<XapiStatement> = emptyList(),
    val publications: Map<String, OpdsPublication> = emptyMap(),
)

class BookmarkListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    private val _uiState = MutableStateFlow(BookmarkListUiState())

    val uiState = _uiState.asStateFlow()

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()

    private val schoolUrl = accountManager.requireActiveSchoolUrl()

    private val agent = XapiAgent(
        account = XapiAccount(
            homePage = schoolUrl.toString(),
            name = requireNotNull(accountManager.activeAccount?.userGuid) {
                "BookmarkListViewModel: active account userGuid must not be null"
            },
        )
    )

    init {
        _appUiState.update {
            it.copy(title = Res.string.home.asUiText())
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    agent = agent,
                    verb = XapiVerb.ID_BOOKMARKED,
                ),
                dataLoadParams = DataLoadParams(),
            ).collect { result ->
                val statements = result.dataOrNull()?.statements
                    ?.sortedBy { it.timestamp }
                    ?: emptyList()

                val publications = loadPublications(statements)

                _uiState.update {
                    it.copy(
                        statements = statements,
                        publications = publications,
                    )
                }
            }
        }
    }

    private suspend fun loadPublications(
        statements: List<XapiStatement>
    ): Map<String, OpdsPublication> = coroutineScope {
        statements.mapNotNull { stmt ->
            val activityId = (stmt.`object` as? XapiActivity)?.id
            if (activityId == null) {
                Napier.w("Bookmark statement ${stmt.id} has non-Activity object, skipping")
                return@mapNotNull null
            }

            async {
                val publication = try {
                    schoolDataSource.opdsPublicationDataSource.getByUrl(
                        url = Url(activityId),
                        params = DataLoadParams(),
                        referrerUrl = null,
                        expectedPublicationId = null,
                    ).dataOrNull()?.resolve(Url(activityId))
                } catch (e: Throwable) {
                    Napier.w("Failed to load publication for bookmark $activityId", e)
                    null
                }
                activityId to publication
            }
        }.awaitAll().mapNotNull { (id, pub) ->
            pub?.let { id to it }
        }.toMap()
    }

    fun onClickRemoveBookmark(statement: XapiStatement) {
        viewModelScope.launch {
            val stmtId = statement.id
            if (stmtId == null) {
                Napier.w("Cannot remove bookmark: statement has no id")
                return@launch
            }

            schoolDataSource.xapiResource.statements.post(
                listOf(
                    XapiStatement(
                        actor = agent,
                        verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                        `object` = XapiStatementRef(id = stmtId.toString()),
                    )
                )
            )

            _uiState.update {
                it.copy(
                    statements = it.statements.filterNot { s -> s.id == stmtId }
                )
            }

            snackBarDispatcher.showSnackBar(
                Snack(
                    message = Res.string.remove_bookmark.asUiText(),
                )
            )
        }
    }

    fun onClickBookmark(statement: XapiStatement) {
        val activityId = (statement.`object` as? XapiActivity)?.id
        if (activityId == null) {
            Napier.w("Cannot navigate to bookmark: statement object is not an Activity")
            return
        }

        _navCommandFlow.tryEmit(
            value = NavCommand.Navigate(
                LearningUnitDetail.create(
                    learningUnitManifestUrl = Url(activityId),
                )
            )
        )
    }
}
