package world.respect.shared.viewmodel.clazz.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.oneroster.rostering.OneRosterRosterDataSource
import world.respect.datalayer.oneroster.rostering.model.OneRosterClass
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.edit_clazz
import world.respect.shared.generated.resources.add_clazz
import world.respect.shared.generated.resources.required
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ClazzEditUiState(
    val entity: OneRosterClass? = null,
    val clazzNameError: String? = null,
)

@OptIn(ExperimentalTime::class)
class ClazzEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val oneRosterDataSource: OneRosterRosterDataSource,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(ClazzEditUiState())

    val uiState = _uiState.asStateFlow()

    private val route: ClazzEdit = savedStateHandle.toRoute()

    init {

        viewModelScope.launch {
            val entity = if (route.sourcedId != null) {
                oneRosterDataSource.getClassBySourcedId(route.sourcedId)
            } else {
                OneRosterClass(
                    sourcedId = UUID.randomUUID().toString(),
                    title = "",
                    dateLastModified = Clock.System.now()
                )
            }

            _uiState.update { it.copy(entity = entity) }

            _appUiState.update { prev ->
                prev.copy(
                    title = if (route.modeEdit) {
                        Res.string.edit_clazz.asUiText()
                    } else {
                        Res.string.add_clazz.asUiText()
                    },
                    userAccountIconVisible = false,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = Res.string.save.asUiText(),
                        onClick = {
                            onSaveClass()
                        }
                    ),
                )
            }
        }
    }


    @OptIn(ExperimentalTime::class)
    fun onSaveClass() {
        val initEntity = _uiState.value.entity ?: return
        viewModelScope.launch {
            if (initEntity.title.isBlank()) {
                _uiState.update { prev ->
                    prev.copy(clazzNameError = getString(Res.string.required))
                }
                return@launch
            }

            val updatedEntity = initEntity.copy(
                dateLastModified = Clock.System.now()
            )

            oneRosterDataSource.putClass(updatedEntity)
        }
    }

    fun onClazzChanged(entity: OneRosterClass?) {
        _uiState.update {
            it.copy(entity = entity, clazzNameError = null)
        }
    }
}