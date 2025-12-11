package world.respect.shared.viewmodel.curriculum.mapping.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.ext.map
import world.respect.lib.opds.model.findIcons
import world.respect.libutil.ext.resolve
import world.respect.shared.navigation.CurriculumMappingDetail
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.curriculum.mapping.edit.CurriculumMappingEditViewModel
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSectionLink

data class CurriculumMappingDetailUiState(
    val mapping: CurriculumMapping? = null,
)

data class CurriculumMappingLessonUiState(
    val icon: Url? = null,
)

class CurriculumMappingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val json: Json,
    private val resultReturner: NavResultReturner,
    private val respectAppDataSource: RespectAppDataSource,
) : RespectViewModel(savedStateHandle) {

    private val route: CurriculumMappingDetail = savedStateHandle.toRoute()
    private val mappingUid = route.uid
    private val mappingData = route.mappingData

    private val _uiState = MutableStateFlow(CurriculumMappingDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                userAccountIconVisible = true,
                hideBottomNavigation = false,
            )
        }

        loadMapping()

        viewModelScope.launch {
            resultReturner.resultFlowForKey(
                CurriculumMappingEditViewModel.KEY_SAVED_MAPPING
            ).collect { result ->
                val savedMapping = result.result as? CurriculumMapping
                if (savedMapping != null && savedMapping.uid == mappingUid) {
                    updateMapping(savedMapping)
                }
            }
        }
    }

    private fun loadMapping() {
        if (mappingData != null) {
            updateMapping(mappingData)
        }
    }

    private fun updateMapping(mapping: CurriculumMapping) {
        _uiState.update {
            it.copy(mapping = mapping)
        }

        _appUiState.update { prev ->
            prev.copy(
                title = mapping.title.asUiText()
            )
        }
    }

    fun lessonUiStateFor(
        link: CurriculumMappingSectionLink
    ): Flow<DataLoadState<CurriculumMappingLessonUiState>> {
        val publicationUrl = Url(link.href)
        return respectAppDataSource.opdsDataSource.loadOpdsPublication(
            url = publicationUrl,
            params = DataLoadParams(),
            referrerUrl = null,
            expectedPublicationId = null,
        ).map { opdsLoadState ->
            opdsLoadState.map { publication ->
                CurriculumMappingLessonUiState(
                    icon = publication.findIcons().firstOrNull()?.let {
                        publicationUrl.resolve(it.href)
                    }
                )
            }
        }
    }

    fun onClickEdit() {
        val mapping = _uiState.value.mapping ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                CurriculumMappingEdit.create(
                    uid = mapping.uid,
                    mappingData = mapping
                )
            )
        )
    }

    fun onClickShare() {
        // TODO: Implement share functionality
    }

    fun onClickCopyPlaylist() {
        // TODO: Implement copy playlist functionality
    }

    fun onClickAssign() {
        // TODO: Implement assign functionality
    }

    fun onClickDelete() {
        // TODO: Implement delete functionality
    }

    fun onClickLesson(lessonHref: String) {
        // TODO: Implement lesson click functionality
    }
}