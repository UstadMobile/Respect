package world.respect.shared.viewmodel.curriculum.mapping.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditViewModel
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_unexpected_result_type
import world.respect.shared.resources.UiText

data class PlaylistListUiState(
    val mappings: List<CurriculumMapping> = emptyList(),
    val selectedFilterIndex: Int = 0,
    val error: UiText? = null,
    val currentUserGuid: String? = null,
    val currentSchoolUrl: Url? = null,
) {
    val filteredMappings: List<CurriculumMapping>
        get() = when (selectedFilterIndex) {
            0 -> mappings
            1 -> mappings.filter { mapping ->
                mapping.isSchoolWide &&
                        mapping.schoolUrl == currentSchoolUrl &&
                        mapping.createdBy != currentUserGuid
            }
            2 -> mappings.filter { mapping ->
                mapping.createdBy == currentUserGuid
            }
            else -> mappings
        }
}

class PlaylistListViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(PlaylistListUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                _uiState.update { prev ->
                    prev.copy(
                        currentUserGuid = sessionAndPerson?.person?.guid,
                        currentSchoolUrl = sessionAndPerson?.session?.account?.school?.self
                    )
                }
            }
        }

        viewModelScope.launch {
            resultReturner.filteredResultFlowForKey(
                CurriculumMappingEditViewModel.KEY_SAVED_MAPPING
            ).collect { result ->
                val savedMapping = result.result as? CurriculumMapping
                if (savedMapping == null) {
                    _uiState.update {
                        it.copy(error = Res.string.error_unexpected_result_type.asUiText())
                    }
                    return@collect
                }
                addOrUpdateMapping(savedMapping)
            }
        }
    }

    fun setMappings(mappings: List<CurriculumMapping>) {
        _uiState.update { it.copy(mappings = mappings) }
    }

    fun onFilterSelected(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
    }
    fun onClickMapping(mapping: CurriculumMapping) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail.createFromMapping(mapping)
            )
        )
    }
    fun onClickAddNew() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                CurriculumMappingEdit.create(uid = 0L, mappingData = null)
            )
        )
    }

    fun onClickAddLink() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnterLink.create()
            )
        )
    }

    fun removeMapping(mapping: CurriculumMapping) {
        val updated = _uiState.value.mappings.filter { it.uid != mapping.uid }
        _uiState.update { it.copy(mappings = updated) }
    }

    private fun addOrUpdateMapping(mapping: CurriculumMapping) {
        val currentMappings = _uiState.value.mappings.toMutableList()
        val existingIndex = currentMappings.indexOfFirst { it.uid == mapping.uid }

        if (existingIndex >= 0) {
            currentMappings[existingIndex] = mapping
        } else {
            currentMappings.add(mapping)
        }

        _uiState.update { it.copy(mappings = currentMappings) }
    }
}