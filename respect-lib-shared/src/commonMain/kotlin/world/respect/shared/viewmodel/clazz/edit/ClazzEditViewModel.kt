package world.respect.shared.viewmodel.clazz.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.oneroster.model.OneRosterClass
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.isReadyAndSettled
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.edit_clazz
import world.respect.shared.generated.resources.add_clazz
import world.respect.shared.generated.resources.required
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.getValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ClazzEditUiState(
    val clazzNameError: String? = null,
    val clazz: DataLoadState<OneRosterClass> = DataLoadingState(),
) {
    val fieldsEnabled: Boolean
        get() = clazz.isReadyAndSettled()
}

@OptIn(ExperimentalTime::class)
class ClazzEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val route: ClazzEdit = savedStateHandle.toRoute()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val sourcedId = route.sourcedId ?: schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        OneRosterClass.TABLE_ID
    ).toString()

    private val _uiState = MutableStateFlow(ClazzEditUiState())

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if (route.sourcedId == null) {
                    Res.string.add_clazz.asUiText()
                } else {
                    Res.string.edit_clazz.asUiText()
                },
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave
                ),
            )
        }

        viewModelScope.launch {

            if (route.sourcedId != null) {
                loadEntity(
                    json = json,
                    serializer = OneRosterClass.serializer(),
                    loadFn = { params ->
                        schoolDataSource.onRoasterDataSource.findClassBySourcedId(params, sourcedId)
                    },
                    uiUpdateFn = { clazz ->
                        _uiState.update { prev -> prev.copy(clazz = clazz) }
                    }
                )
            } else {
                _uiState.update { prev ->
                    prev.copy(
                        clazz = DataReadyState(
                            OneRosterClass(
                                sourcedId = sourcedId,
                                title = "",
                                dateLastModified = Clock.System.now()
                            )
                        )
                    )
                }
            }
        }
    }

    fun onEntityChanged(clazz: OneRosterClass) {
        val classToCommit = _uiState.updateAndGet { prev ->
            prev.copy(clazz = DataReadyState(clazz))
        }.clazz.dataOrNull() ?: return

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(classToCommit)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun onClickSave() {
        val clazz = _uiState.value.clazz.dataOrNull() ?: return
        if (clazz.title.isBlank()) {
            _uiState.update { prev ->
                prev.copy(clazzNameError = Res.string.required.asUiText().toString()) // or a hardcoded "Required"
            }
            return
        } else {
            _uiState.update { prev -> prev.copy(clazzNameError = null) }
        }

        viewModelScope.launch {
            try {
                schoolDataSource.onRoasterDataSource.putClass(clazz)
                if (route.sourcedId == null) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            ClazzDetail(sourcedId), popUpTo = route, popUpToInclusive = true
                        )
                    )
                } else {
                    _navCommandFlow.tryEmit(NavCommand.PopUp())
                }
            } catch (e: Throwable) {
                //needs to display snack bar here

            }
        }
    }
    fun onClearError() {
        _uiState.update { prev -> prev.copy(clazzNameError = null) }
    }

}