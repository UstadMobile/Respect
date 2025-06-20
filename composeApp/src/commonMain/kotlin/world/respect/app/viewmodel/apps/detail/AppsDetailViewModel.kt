package world.respect.app.viewmodel.apps.detail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import world.respect.app.model.appsdetail.AppsDetailModel
import world.respect.app.viewmodel.RespectViewModel
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.apps_detail
import world.respect.app.model.appsdetail.Images
import world.respect.datasource.opds.model.LangMapStringValue
import world.respect.datasource.opds.model.OpdsPublication
import world.respect.datasource.opds.model.ReadiumContributorStringValue
import world.respect.datasource.opds.model.ReadiumLink
import world.respect.datasource.opds.model.ReadiumMetadata
import world.respect.datasource.opds.model.ReadiumSubjectStringValue


data class AppsDetailUiState(
    val appsDetailData: AppsDetailModel? = null
)

class AppsDetailViewModel : RespectViewModel() {
    private val _uiState = MutableStateFlow(AppsDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(title = getString(resource = Res.string.apps_detail))
            }
        }
        loaddata()
    }

    //mock data
    private fun loaddata() {
        val appsDetailData = AppsDetailModel(
            imageName = "Chimple",
            appName = "Chimple: Kids",
            appDescription = "Chimple kids is an educational app to learn basic reading, writing & math skills",
            publications = listOf(
                OpdsPublication(
                    metadata = ReadiumMetadata(
                        title = LangMapStringValue("Lesson 001"),
                        author = listOf(
                            ReadiumContributorStringValue("Mullah Nasruddin")
                        ),
                        language = listOf("en"),
                        modified = "2015-09-29T17:00:00Z",
                        subject = listOf(
                            ReadiumSubjectStringValue("English"),
                        ),
                        duration = 2.0

                    ),
                    links = listOf(
                        ReadiumLink(
                            href = "",
                            type = "application/opds-publication+json",
                            rel = listOf("self")
                        ),
                        ReadiumLink(
                            href = "",
                            type = "text/html",
                            rel = listOf("http://opds-spec.org/acquisition/open-access")
                        )
                    ),
                    images = listOf(
                        ReadiumLink(
                            href = "",
                            type = "image/jpeg",
                            height = 700,
                            width = 400
                        )
                    )
                ),
                OpdsPublication(
                    metadata = ReadiumMetadata(
                        title = LangMapStringValue("Lesson 005"),
                        author = listOf(
                            ReadiumContributorStringValue("Mullah Nasruddin")
                        ),
                        language = listOf("en"),
                        modified = "2015-09-29T17:00:00Z",
                        subject = listOf(
                            ReadiumSubjectStringValue("Mathematics"),
                        ),
                        duration = 1.0


                    ),
                    links = listOf(
                        ReadiumLink(
                            href = "",
                            type = "application/opds-publication+json",
                            rel = listOf("self")
                        ),
                        ReadiumLink(
                            href = "",
                            type = "text/html",
                            rel = listOf("http://opds-spec.org/acquisition/open-access")
                        )
                    ),
                    images = listOf(
                        ReadiumLink(
                            href = "",
                            type = "image/jpeg",
                            height = 700,
                            width = 400
                        )
                    )
                )
            ),
            images = listOf(
                Images(""),
                Images(""),
                Images(""),
            )
        )
        _uiState.value = AppsDetailUiState(appsDetailData = appsDetailData)
    }
}
