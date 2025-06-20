package world.respect.app.viewmodel.lessons.detail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.app.viewmodel.RespectViewModel
import world.respect.datasource.opds.model.LangMapStringValue
import world.respect.datasource.opds.model.OpdsPublication
import world.respect.datasource.opds.model.ReadiumContributorStringValue
import world.respect.datasource.opds.model.ReadiumLink
import world.respect.datasource.opds.model.ReadiumMetadata
import world.respect.datasource.opds.model.ReadiumSubjectStringValue

data class LessonDetailUiState(
    val lessonDetailData: OpdsPublication? = null,
    val publications: List<OpdsPublication> = emptyList(),

    )

class LessonDetailViewModel : RespectViewModel() {
    private val _uiState = MutableStateFlow(LessonDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(title ="")
            }
        }
        loaddata()
    }

    private fun loaddata() {
        val lessonDetailData =  OpdsPublication(
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
                duration = 2.0,
                subtitle = LangMapStringValue("Lesson Outcome"),

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
        val publications: List<OpdsPublication> = listOf(
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
        )
        _uiState.value = LessonDetailUiState(
            publications = publications,
                    lessonDetailData = lessonDetailData)
    }
}
