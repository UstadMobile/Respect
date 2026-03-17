package world.respect.shared.viewmodel.schooldirectory.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.shared.domain.appversioninfo.GetAppVersionInfoUseCase
import world.respect.shared.domain.school.LaunchCustomTabUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.school_directories
import world.respect.shared.generated.resources.school_directory
import world.respect.shared.generated.resources.select_host
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryEdit
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import java.net.URLEncoder

data class SchoolDirectoryListUiState(
    val schoolDirectory: List<RespectSchoolDirectory> = emptyList(),
    val mode: SchoolDirectoryMode = SchoolDirectoryMode.MANAGE
)

class SchoolDirectoryListViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAppDataSource: RespectAppDataSource,
    private val launchCustomTabUseCase: LaunchCustomTabUseCase,
    private val getAppVersionInfoUseCase: GetAppVersionInfoUseCase,
) : RespectViewModel(savedStateHandle) {

    private val route: SchoolDirectoryList = savedStateHandle.toRoute()
    private val _uiState = MutableStateFlow(SchoolDirectoryListUiState(mode = route.mode))

    val uiState = _uiState.asStateFlow()

    init {
        // Configure the app bar based on mode
        _appUiState.update {
            it.copy(
                title = when (route.mode) {
                    SchoolDirectoryMode.MANAGE -> Res.string.school_directories.asUiText()
                    SchoolDirectoryMode.SELECT -> Res.string.select_host.asUiText()
                },
                hideBottomNavigation = true,
                fabState = when (route.mode) {
                    SchoolDirectoryMode.MANAGE -> it.fabState.copy(
                        icon = FabUiState.FabIcon.ADD,
                        text = Res.string.school_directory.asUiText(),
                        onClick = ::onClickAdd,
                        visible = true
                    )
                    SchoolDirectoryMode.SELECT -> it.fabState.copy(visible = false)
                }
            )
        }

        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryDataSource.allDirectoriesAsFlow().collect { directories ->
                _uiState.update { prev ->
                    prev.copy(schoolDirectory = directories)
                }
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = SchoolDirectoryEdit,
                popUpTo = SchoolDirectoryList.create(SchoolDirectoryMode.MANAGE)
            )
        )
    }

    fun onDeleteDirectory(directory: RespectSchoolDirectory) {
        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryDataSource.deleteDirectory(directory)
        }
    }

    fun onSelectDirectory(directory: RespectSchoolDirectory) {
        when (route.mode) {
            SchoolDirectoryMode.SELECT -> {
                viewModelScope.launch {
                    // ADD THIS LOG
                    println("MAESTRO_DEBUG: onSelectDirectory called with directory: ${directory.baseUrl}")

                    val appInfo = getAppVersionInfoUseCase()
                    println("MAESTRO_DEBUG: App package name: ${appInfo.packageName}")

                    val encodedPackageName = URLEncoder.encode(appInfo.packageName, "UTF-8")
                    val registrationUrl = "${directory.baseUrl}register-school?packageName=$encodedPackageName"

                    println("MAESTRO_DEBUG: Generated registration URL: $registrationUrl")
                    println("MAESTRO_DEBUG: Attempting to launch custom tab...")

                    try {
                        launchCustomTabUseCase(url = registrationUrl)
                        println("MAESTRO_DEBUG: launchCustomTabUseCase completed successfully")
                    } catch (e: Exception) {
                        println("MAESTRO_DEBUG: ERROR in launchCustomTabUseCase: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
            else -> {
                // ADD THIS LOG TO SEE IF OTHER MODE IS BEING TRIGGERED
                println("MAESTRO_DEBUG: onSelectDirectory called but mode is ${route.mode}, not SELECT")
            }
        }
    }
}
