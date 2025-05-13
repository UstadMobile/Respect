package world.respect.app.viewmodel.applauncher

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.add
import respect.composeapp.generated.resources.apps
import world.respect.app.app.AppList
import world.respect.app.appstate.FabUiState
import world.respect.app.model.applauncher.AppLauncherModel
import world.respect.app.viewmodel.RespectViewModel

data class AppLauncherUiState(
    val appLauncherDataList: List<AppLauncherModel> = emptyList(),
)

class AppLauncherScreenViewModel() : RespectViewModel() {

    private val _uiState = MutableStateFlow(AppLauncherUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = getString(resource = Res.string.apps),
                    showBackButton = false,
                    fabState = FabUiState(
                        visible = true,
                        icon = FabUiState.FabIcon.ADD,
                        text = getString(resource = Res.string.add),
                        onClick = {
                            navController.navigate(AppList)
                        })
                )
            }

        }
        loadAppLauncherData()
    }

    //mock data for testing purpose
    private fun loadAppLauncherData() {
        val appLauncherData: List<AppLauncherModel> =
            listOf(
                AppLauncherModel(
                    imageText = "Chimple",
                    title = "App 1",
                    category = "Education",
                    ageRange = "3-5"
                ),
                AppLauncherModel(
                    imageText = "Ustad  Mobile",
                    title = "App 2",
                    category = "Games",
                    ageRange = "5-7"
                ),
                AppLauncherModel(
                    imageText = "Khan Academy",
                    title = "App 3",
                    category = "Entertainment",
                    ageRange = "7-10"
                ),
                AppLauncherModel(
                    imageText = "Curious Learning",
                    title = "App 4",
                    category = "Productivity",
                    ageRange = "10-12"
                ),
                AppLauncherModel(
                    imageText = "Dals  Learning",
                    title = "App 5",
                    category = "Learning",
                    ageRange = "12+"
                )
            )

        _uiState.value = _uiState.value.copy(
            appLauncherDataList = appLauncherData
        )
    }
}

