package world.respect.app.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.ustadmobile.libuicompose.theme.appBarSelectionModeBackgroundColor
import com.ustadmobile.libuicompose.theme.appBarSelectionModeContentColor
import world.respect.app.appstate.AppBarColors
import world.respect.app.appstate.AppUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespectAppBar(
    compactHeader: Boolean,
    appUiState: AppUiState,
    navController: NavController,
    screenName: String? = null, // <-- Pass screen name if available
    onProfileClick: () -> Unit = {}, // <-- Handle profile icon click
) {

    val title = appUiState.title ?: screenName ?: "Respect"
    val defaultCanGoBack = navController.previousBackStackEntry != null
    val canGoBack = appUiState.showBackButton ?: defaultCanGoBack


    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("app_title"),
            )
        },
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                }
            }
        },
        actions = {
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("profile_icon")
            ) {
                Icon(Icons.Default.Person,
                    contentDescription =null)
            }
        },
        colors = if(appUiState.appBarColors == AppBarColors.STANDARD) {
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        }else {
            val contentColor = MaterialTheme.colorScheme.appBarSelectionModeContentColor
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.appBarSelectionModeBackgroundColor,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor,
            )
        },
    )
}
