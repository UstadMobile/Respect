package world.respect.app.viewmodel.learningunit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.app.app.LearningUnitDetail
import world.respect.app.datasource.RespectAppDataSourceProvider
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.opds.model.OpdsPublication
import world.respect.navigation.NavCommand

data class LearningUnitDetailUiState(
    val lessonDetail: OpdsPublication? = null,
    val publications: List<OpdsPublication> = emptyList()
)

class LearningUnitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    dataSourceProvider: RespectAppDataSourceProvider
) : RespectViewModel(savedStateHandle) {

    private val dataSource = dataSourceProvider.getDataSource(activeAccount)

    private val _uiState = MutableStateFlow(LearningUnitDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LearningUnitDetail = savedStateHandle.toRoute()

    init {

        viewModelScope.launch {
            _appUiState.update {
                it.copy(title = "")
            }
            dataSource.opdsDataSource.loadOpdsPublication(
                url = route.learningUnitManifestUrl,
                params = DataLoadParams(),
                referrerUrl = route.learningUnitManifestUrl,
                expectedPublicationId = route.expectedIdentifier
            ).collect { result ->
                when (result) {
                    is DataLoadResult -> {
                        _uiState.update {
                            it.copy(
                                lessonDetail = result.data,
                            )
                        }
                    }

                    else -> {
                    }
                }
            }
        }
    }

    fun onClickLesson(publication: OpdsPublication) {
        val publicationSelfLink = publication.links.find { it.rel?.equals("self") == true }?.href

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LearningUnitDetail(
                    learningUnitManifestUrl = publicationSelfLink?:"",
                    refererUrl = route.refererUrl,
                    expectedIdentifier = publication.metadata.identifier.toString()
                )
            )
        )
    }

}
