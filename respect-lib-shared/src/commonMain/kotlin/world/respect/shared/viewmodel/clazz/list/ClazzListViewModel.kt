package world.respect.shared.viewmodel.clazz.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.datalayer.oneroster.rostering.FakeRosterDataSource
import world.respect.datalayer.oneroster.rostering.model.OneRosterClass
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.clazz
import world.respect.shared.generated.resources.first_name
import world.respect.shared.generated.resources.last_name
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.ClazzList
import world.respect.shared.navigation.NavCommand
import world.respect.shared.util.SortOrderOption
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
) : RespectViewModel(savedStateHandle) {

    private val fakeRosterDataSource = FakeRosterDataSource
    private val _uiState = MutableStateFlow(ClazzListUiState())

    val uiState = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {

            val classes = fakeRosterDataSource.getAllClasses()

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
                    title = getString(Res.string.classes),
                    showBackButton = false,
                    fabState = FabUiState(
                        visible = true,
                        icon = FabUiState.FabIcon.ADD,
                        text = getString(resource = Res.string.clazz),
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    ClazzEdit.create(
                                        sourcedId = null
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

