package world.respect.shared.viewmodel.learningunit.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libcache.UstadCache
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.map
import world.respect.datalayer.respect.model.LEARNING_UNIT_MIME_TYPES
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.findIcons
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.CurriculumMappingEdit
import world.respect.shared.navigation.LearningUnitDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResult
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PlaylistShare
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditViewModel
import world.respect.shared.viewmodel.playlists.mapping.edit.PlaylistEditViewModel
import world.respect.shared.viewmodel.playlists.mapping.edit.PlaylistSectionUiState
import world.respect.shared.viewmodel.playlists.mapping.model.PlaylistsMapping
import world.respect.shared.viewmodel.playlists.mapping.model.PlaylistsMappingSectionLink
import world.respect.shared.viewmodel.learningunit.LearningUnitSelection

data class LearningUnitDetailUiState(
    val lessonDetail: OpdsPublication? = null,
    val app: DataLoadState<RespectAppManifest> = DataLoadingState(),
    val pinState: PublicationPinState = PublicationPinState(
        PublicationPinState.Status.NOT_PINNED, 0, 0
    ),
    val mapping: PlaylistsMapping? = null,
    val sectionLinkUiState: (PlaylistsMappingSectionLink) -> Flow<DataLoadState<PlaylistSectionUiState>> = {
        emptyFlow()
    },
    val showCopyDialog: Boolean = false,
    val copyDialogName: String = "",
    val isSelectionMode: Boolean = false,
    val selectedLessons: Set<PlaylistsMappingSectionLink> = emptySet(),
) {
    val buttonsEnabled: Boolean
        get() = lessonDetail != null || mapping != null

    val showEditButton: Boolean
        get() = mapping != null && !isSelectionMode
}

class LearningUnitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDataSource: RespectAppDataSource,
    private val launchAppUseCase: LaunchAppUseCase,
    private val ustadCache: UstadCache,
    private val resultReturner: NavResultReturner,
    private val json: Json,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(LearningUnitDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: LearningUnitDetail = savedStateHandle.toRoute()

    init {
        val mappingData = route.mappingData
        val isSelectionMode = route.isSelectionMode

        if (mappingData != null) {
            _uiState.update {
                it.copy(
                    mapping = mappingData,
                    sectionLinkUiState = this@LearningUnitDetailViewModel::sectionLinkUiStateFor,
                    isSelectionMode = isSelectionMode
                )
            }
            _appUiState.update {
                it.copy(
                    title = mappingData.title.asUiText(),
                    hideBottomNavigation = isSelectionMode,
                    showBackButton = true,
                )
            }
        } else {
            viewModelScope.launch {
                appDataSource.opdsDataSource.loadOpdsPublication(
                    url = route.learningUnitManifestUrl,
                    params = DataLoadParams(),
                    referrerUrl = route.learningUnitManifestUrl,
                    expectedPublicationId = route.expectedIdentifier
                ).collect { result ->
                    when (result) {
                        is DataReadyState -> {
                            _uiState.update {
                                it.copy(
                                    lessonDetail = result.data.resolve(
                                        route.learningUnitManifestUrl
                                    )
                                )
                            }

                            _appUiState.update {
                                it.copy(
                                    title = result.data.metadata.title.getTitle().asUiText(),
                                    showBackButton = true,
                                    hideBottomNavigation = false
                                )
                            }
                        }
                        else -> {
                        }
                    }
                }
            }

            viewModelScope.launch {
                appDataSource.compatibleAppsDataSource.getAppAsFlow(
                    manifestUrl = route.appManifestUrl,
                    loadParams = DataLoadParams()
                ).collect { app ->
                    _uiState.update { it.copy(app = app) }
                }
            }

            viewModelScope.launch {
                ustadCache.publicationPinState(route.learningUnitManifestUrl).collect { pinState ->
                    _uiState.update { it.copy(pinState = pinState) }
                }
            }
        }
    }

    fun onClickOpen() {
        val respectApp = _uiState.value.app.dataOrNull() ?: return
        val launchLink = _uiState.value.lessonDetail?.links?.firstOrNull { link ->
            link.rel?.any { it.startsWith("http://opds-spec.org/acquisition") } == true &&
                    LEARNING_UNIT_MIME_TYPES.any { link.type?.startsWith(it) == true }
        } ?: return

        val launchUrl = route.learningUnitManifestUrl.resolve(launchLink.href)

        launchAppUseCase(
            app = respectApp,
            learningUnitId = launchUrl,
            navigateFn = {
                _navCommandFlow.tryEmit(it)
            }
        )
    }

    fun onClickDownload() {
        viewModelScope.launch {
            try {
                when(uiState.value.pinState.status) {
                    PublicationPinState.Status.NOT_PINNED -> {
                        ustadCache.pinPublication(route.learningUnitManifestUrl)
                    }
                    PublicationPinState.Status.READY -> {
                        ustadCache.unpinPublication(route.learningUnitManifestUrl)
                    }
                    else -> {
                        //Do nothing
                    }
                }
            } catch(t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private suspend fun loadLessonPublications(
        lessons: List<PlaylistsMappingSectionLink>
    ): List<LearningUnitSelection> {
        return lessons.mapNotNull { lesson ->
            try {
                val publicationState = appDataSource.opdsDataSource.loadOpdsPublication(
                    url = Url(lesson.href),
                    params = DataLoadParams(),
                    referrerUrl = null,
                    expectedPublicationId = null,
                ).first { it is DataReadyState }

                val publication = (publicationState as? DataReadyState)?.data

                if (publication != null && lesson.appManifestUrl != null) {
                    LearningUnitSelection(
                        learningUnitManifestUrl = Url(lesson.href),
                        selectedPublication = publication,
                        appManifestUrl = lesson.appManifestUrl
                    )
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getAvailablePlaylists(): List<PlaylistsMapping> {
        val mappingsJson = savedStateHandle.get<String>(AppLauncherViewModel.KEY_MAPPINGS_LIST)
        return if (mappingsJson != null) {
            try {
                json.decodeFromString(
                    ListSerializer(PlaylistsMapping.serializer()),
                    mappingsJson
                )
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun onClickAssign() {
        val mapping = uiState.value.mapping
        val availablePlaylists = getAvailablePlaylists()

        if (mapping != null) {
            val allLessons = mapping.sections.flatMap { section ->
                section.items.filter { it.appManifestUrl != null }
            }

            if (allLessons.isNotEmpty()) {
                viewModelScope.launch {
                    val learningUnitSelections = loadLessonPublications(allLessons)

                    if (learningUnitSelections.isNotEmpty()) {
                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                destination = AssignmentEdit.createWithMultipleLessons(
                                    uid = null,
                                    learningUnits = learningUnitSelections,
                                    availablePlaylists = availablePlaylists
                                )
                            )
                        )
                    }
                }
            } else {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = AssignmentEdit.create(
                            uid = null,
                            learningUnitSelected = null,
                            availablePlaylists = availablePlaylists
                        )
                    )
                )
            }
        } else {
            val publicationVal = uiState.value.lessonDetail ?: return
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = AssignmentEdit.create(
                        uid = null,
                        learningUnitSelected = LearningUnitSelection(
                            learningUnitManifestUrl = route.learningUnitManifestUrl,
                            selectedPublication = publicationVal,
                            appManifestUrl = route.appManifestUrl,
                        ),
                        availablePlaylists = availablePlaylists
                    )
                )
            )
        }
    }

    fun onClickAssignSection(sectionUid: Long) {
        val mapping = _uiState.value.mapping ?: return
        val section = mapping.sections.find { it.uid == sectionUid } ?: return
        val sectionLessons = section.items.filter { it.appManifestUrl != null }
        val availablePlaylists = getAvailablePlaylists()

        if (sectionLessons.isNotEmpty()) {
            viewModelScope.launch {
                val learningUnitSelections = loadLessonPublications(sectionLessons)

                if (learningUnitSelections.isNotEmpty()) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            destination = AssignmentEdit.createWithMultipleLessons(
                                uid = null,
                                learningUnits = learningUnitSelections,
                                availablePlaylists = availablePlaylists
                            )
                        )
                    )
                }
            }
        }
    }

    fun onLessonSelectionToggle(link: PlaylistsMappingSectionLink) {
        _uiState.update { prev ->
            val selected = if (prev.selectedLessons.contains(link)) {
                prev.selectedLessons - link
            } else {
                prev.selectedLessons + link
            }
            prev.copy(selectedLessons = selected)
        }
    }

    fun onClickSelectAll() {
        val mapping = _uiState.value.mapping ?: return
        val allLessons = mapping.sections.flatMap { it.items }.toSet()
        _uiState.update { it.copy(selectedLessons = allLessons) }
    }

    fun onClickSelectNone() {
        _uiState.update { it.copy(selectedLessons = emptySet()) }
    }

    fun onClickToggleSectionSelection(sectionUid: Long) {
        val mapping = _uiState.value.mapping ?: return
        val section = mapping.sections.find { it.uid == sectionUid } ?: return
        val sectionLessons = section.items

        val allSelected = sectionLessons.all { _uiState.value.selectedLessons.contains(it) }

        _uiState.update { prev ->
            val newSelection = if (allSelected) {
                prev.selectedLessons - sectionLessons.toSet()
            } else {
                prev.selectedLessons + sectionLessons.toSet()
            }
            prev.copy(selectedLessons = newSelection)
        }
    }

    fun onConfirmSelection() {
        val selectedLessons = _uiState.value.selectedLessons.toList()

        if (selectedLessons.isEmpty()) return

        viewModelScope.launch {
            val learningUnitSelections = loadLessonPublications(selectedLessons)

            if (learningUnitSelections.isNotEmpty()) {
                resultReturner.sendResult(
                    NavResult(
                        key = AssignmentEditViewModel.KEY_PLAYLIST_SELECTION,
                        result = learningUnitSelections
                    )
                )
                _navCommandFlow.tryEmit(NavCommand.PopUp())
            }
        }
    }

    fun onClickLesson(link: PlaylistsMappingSectionLink) {
        if (_uiState.value.isSelectionMode) {
            onLessonSelectionToggle(link)
        } else {
            val publicationUrl = Url(link.href)
            val appManifestUrl = link.appManifestUrl ?: return

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    LearningUnitDetail.create(
                        learningUnitManifestUrl = publicationUrl,
                        appManifestUrl = appManifestUrl,
                        refererUrl = publicationUrl,
                        expectedIdentifier = null
                    )
                )
            )
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
        val mapping = _uiState.value.mapping ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PlaylistShare.create(playlistUid = mapping.uid)
            )
        )
    }

    fun onClickCopy() {
        _uiState.update { it.copy(showCopyDialog = true) }
    }

    fun onCopyDialogDismiss() {
        _uiState.update { it.copy(showCopyDialog = false, copyDialogName = "") }
    }

    fun onCopyDialogNameChanged(name: String) {
        _uiState.update { it.copy(copyDialogName = name) }
    }

    fun onCopyDialogConfirm() {
        val mapping = _uiState.value.mapping ?: return
        val newName = _uiState.value.copyDialogName.trim()

        if (newName.isEmpty()) {
            return
        }

        val copiedMapping = mapping.copy(
            uid = System.currentTimeMillis(),
            title = newName
        )

        resultReturner.sendResult(
            NavResult(
                key = PlaylistEditViewModel.KEY_SAVED_MAPPING,
                result = copiedMapping
            )
        )

        _uiState.update {
            it.copy(
                showCopyDialog = false,
                copyDialogName = ""
            )
        }
    }

    fun onClickDelete() {
        // TODO:
    }

    fun sectionLinkUiStateFor(
        link: PlaylistsMappingSectionLink
    ): Flow<DataLoadState<PlaylistSectionUiState>> {
        val publicationUrl = Url(link.href)
        return appDataSource.opdsDataSource.loadOpdsPublication(
            url = publicationUrl,
            params = DataLoadParams(),
            referrerUrl = null,
            expectedPublicationId = null,
        ).map { opdsLoadState ->
            opdsLoadState.map { publication ->
                PlaylistSectionUiState(
                    icon = publication.findIcons().firstOrNull()?.let {
                        publicationUrl.resolve(it.href)
                    },
                    title = publication.metadata.title.getTitle(),
                    subtitle = publication.metadata.subtitle?.getTitle().orEmpty(),
                    description = publication.metadata.description.orEmpty()
                )
            }
        }
    }
}