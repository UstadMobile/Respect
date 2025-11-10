package world.respect.shared.viewmodel.enrollment.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.isReadyAndSettled
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.EnrollmentEdit
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.getValue

data class EnrollmentEditUiState(
    val enrollment: DataLoadState<Enrollment> = DataLoadingState(),
    val startDateError: String? = null,
    val endDateError: String? = null
) {
    val fieldsEnabled: Boolean
        get() = enrollment.isReadyAndSettled()
}

class EnrollmentEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireSelectedAccountScope()
    val route: EnrollmentEdit = savedStateHandle.toRoute()
    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(EnrollmentEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    onClick = ::onClickSave,
                    text = Res.string.save.asUiText(),
                    visible = true,
                )
            )

        }
        launchWithLoadingIndicator {
            if (route.uid != null) {
                loadEntity(
                    json = json,
                    serializer = Enrollment.serializer(),
                    loadFn = { params ->
                        schoolDataSource.enrollmentDataSource.findByGuid(params, route.uid)
                    },
                    uiUpdateFn = { enrollment ->
                        _uiState.update {
                            it.copy(enrollment = enrollment)
                        }
                    }
                )
            } else {
                _uiState.update { prev ->
                    prev.copy(
                        enrollment = DataReadyState(
                            Enrollment(
                                uid = route.uid.toString(),
                                classUid = route.clazzGuid,
                                personUid = route.personGuid,
                                role = EnrollmentRoleEnum.valueOf(route.role),
                            )
                        )
                    )
                }
            }
        }
    }



    fun onClickSave() {
    }
}