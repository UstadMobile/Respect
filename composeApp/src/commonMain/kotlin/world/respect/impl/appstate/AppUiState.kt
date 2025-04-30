package world.respect.impl.appstate

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
    )