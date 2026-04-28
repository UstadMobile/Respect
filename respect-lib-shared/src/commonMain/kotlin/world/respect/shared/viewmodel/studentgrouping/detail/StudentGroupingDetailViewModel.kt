package world.respect.shared.viewmodel.studentgrouping.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.xapi.model.XapiGroup.Companion.RESULT_KEY_GROUP_UPDATED
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_group
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.StudentGroupingDetail
import world.respect.shared.navigation.StudentGroupingEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue


data class StudentGroupingDetailUiState(
    val groupName: String = "",
    val groupMembers: List<String> = emptyList()
)

class StudentGroupingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    respectAccountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = respectAccountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(StudentGroupingDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: StudentGroupingDetail = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                fabState = FabUiState(
                    visible = true,
                    icon = FabUiState.FabIcon.EDIT,
                    text = Res.string.edit.asUiText(),
                    onClick = ::onClickEdit
                )
            )
        }
        loadGroupDetail()

        // Listen for result from edit screen and refresh
        val navResultReturner: NavResultReturner = getKoin().get()
        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(
                RESULT_KEY_GROUP_UPDATED
            ).collect {
                loadGroupDetail()
            }
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                StudentGroupingEdit(
                    classUid = route.classId,
                    groupId = route.groupId
                )
            )
        )
    }

    private fun loadGroupDetail() {
        viewModelScope.launch {
            try {
                val group = schoolDataSource.xapiActorDataSource.getGroupDetail(route.groupId)

                if (group != null) {
                    val memberNames = group.member?.mapNotNull { it.name } ?: emptyList()

                    _uiState.update { prev ->
                        prev.copy(
                            groupName = group.name ?: "",
                            groupMembers = memberNames
                        )
                    }
                }

                _appUiState.update {
                    it.copy(
                        title = group?.name?.asUiText() ?: Res.string.create_group.asUiText(),
                        userAccountIconVisible = false,
                        hideBottomNavigation = true,
                    )
                }
            } catch (e: Throwable) {
                Napier.e("loadGroupDetail ERROR", throwable = e)
            }
        }
    }
}
