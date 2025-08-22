package world.respect.shared.viewmodel.clazz.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.oneroster.rostering.OneRosterRosterDataSource
import world.respect.datalayer.oneroster.rostering.model.OneRosterClass
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.clazz
import world.respect.shared.generated.resources.first_name
import world.respect.shared.generated.resources.last_name
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

data class ClazzListUiState(
    val oneRoasterClass: List<OneRosterClass> = emptyList(),
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.first_name, 1, true
    ),
    val fieldsEnabled: Boolean = true
)

class ClazzListViewModel(
    savedStateHandle: SavedStateHandle,
    private val oneRosterDataSource: OneRosterRosterDataSource,
) : RespectViewModel(savedStateHandle) {
    private val _uiState = MutableStateFlow(ClazzListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {

            val classes = oneRosterDataSource.getAllClasses()

            _uiState.update {
                val sortOptions = listOf(
                    SortOrderOption(
                        Res.string.first_name,
                        flag = 1,
                        order = true
                    ),
                    SortOrderOption(
                        Res.string.last_name,
                        flag = 2,
                        order = true
                    )
                )

                it.copy(
                    oneRoasterClass = classes,
                    sortOptions = sortOptions,
                    activeSortOrderOption = sortOptions.first()
                )
            }

            _appUiState.update {
                it.copy(
                    title = Res.string.classes.asUiText(),
                    showBackButton = false,
                    fabState = FabUiState(
                        visible = true,
                        icon = FabUiState.FabIcon.ADD,
                        text = Res.string.clazz.asUiText(),
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    ClazzEdit.create(
                                        sourcedId = null,
                                        modeEdit = false
                                    )
                                )
                            )
                        }
                    )
                )
            }
        }
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update {
            it.copy(activeSortOrderOption = sortOption)
        }
    }

    fun onClickClazz(sourcedId: String) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ClazzDetail.create(sourcedId)
            )
        )
    }
}

