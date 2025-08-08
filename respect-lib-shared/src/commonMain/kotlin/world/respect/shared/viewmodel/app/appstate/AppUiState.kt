package world.respect.shared.viewmodel.app.appstate

data class AppUiState(
    val fabState: FabUiState = FabUiState(),
    val title: String? = null,
    val navigationVisible: Boolean = true,
    val hideBottomNavigation: Boolean = false,
    val hideSettingsIcon: Boolean = false,
    val userAccountIconVisible: Boolean = true,
    val hideAppBar: Boolean = false,
    val appBarColors: AppBarColors = AppBarColors.STANDARD,
    val leadingActionButton: AppActionButton? = null,
    val loadingState: LoadingUiState = LoadingUiState(),
    val showBackButton: Boolean? = true,
    val searchState: AppBarSearchUiState = AppBarSearchUiState(),
    val actionBarButtonState: ActionBarButtonUiState = ActionBarButtonUiState(),
    )