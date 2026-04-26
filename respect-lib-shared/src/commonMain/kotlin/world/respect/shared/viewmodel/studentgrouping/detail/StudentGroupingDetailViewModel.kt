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
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_group
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.StudentGroupingDetail
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
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
        loadGroupDetail()
    }

    private fun loadGroupDetail() {
        viewModelScope.launch {
            try {
                val group = schoolDataSource.xapiActorDataSource.getGroupDetail(route.groupId)

                if (group != null) {
                    // Extract member names from the group
                    val memberNames = group.member?.mapNotNull { it.name } ?: emptyList()

                    // Print group details and members
                    Napier.i("Group Name: ${group.name}")
                    Napier.i("Number of Members: ${memberNames.size}")
                    memberNames.forEachIndexed { index, memberName ->
                        Napier.i("Member ${index + 1}: $memberName")
                    }

                    // Update UI state with group information
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

    fun onClickBack() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ClazzDetail(route.classId), popUpTo = route, popUpToInclusive = true
            )
        )
    }
}

