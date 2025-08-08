package world.respect.app.app

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ustadmobile.libuicompose.theme.appBarSelectionModeBackgroundColor
import com.ustadmobile.libuicompose.theme.appBarSelectionModeContentColor
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.search
import world.respect.shared.viewmodel.app.appstate.AppBarColors
import world.respect.shared.viewmodel.app.appstate.AppUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespectAppBar(
    compactHeader: Boolean,
    appUiState: AppUiState,
    navController: NavController,
    screenName: String? = null, // <-- Pass screen name if available
    onProfileClick: () -> Unit = {}, // <-- Handle profile icon click
) {

    val title = appUiState.title ?: screenName ?: ""
    val defaultCanGoBack = navController.previousBackStackEntry != null
    val canGoBack = appUiState.showBackButton ?: defaultCanGoBack

    var searchActive by remember {
        mutableStateOf(false)
    }

    var searchHasFocus by remember {
        mutableStateOf(false)
    }

    val focusRequester = remember { FocusRequester() }

    //Focus the search box when it appears after the user clicks the search icon
    LaunchedEffect(searchActive) {
        if(compactHeader && searchActive)
            focusRequester.requestFocus()
    }
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
            if(appUiState.searchState.visible) {
                if(!compactHeader || searchActive) {
                    OutlinedTextField(
                        modifier = Modifier.testTag("search_box")
                            .focusRequester(focusRequester)
                            .let {
                                if(compactHeader || searchHasFocus) {
                                    it.width(320.dp)
                                }else {
                                    it.width(192.dp)
                                }
                            }
                            .onFocusChanged {
                                searchHasFocus = it.hasFocus
                            },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if(searchActive) {
                                IconButton(
                                    modifier = Modifier.testTag("close_search_button"),
                                    onClick = {
                                        appUiState.searchState.onSearchTextChanged("")
                                        searchActive = false
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "")
                                }
                            }
                        },
                        value = appUiState.searchState.searchText,
                        placeholder = {
                            Text(text = stringResource(resource = Res.string.search))
                        },
                        onValueChange = appUiState.searchState.onSearchTextChanged,
                    )
                }else {
                    IconButton(
                        modifier = Modifier.testTag("expand_search_icon_button"),
                        onClick = {
                            searchActive = true
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription =
                            stringResource(Res.string.search)
                        )
                    }
                }
            }
            if(appUiState.actionBarButtonState.visible) {
                Button(
                    onClick = appUiState.actionBarButtonState.onClick,
                    enabled = appUiState.actionBarButtonState.enabled,
                    modifier = Modifier.testTag("action_bar_button"),
                ) {
                    Text(appUiState.actionBarButtonState.text ?: "")
                }
            }

            if(appUiState.userAccountIconVisible) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.testTag("profile_icon")
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null
                    )
                }
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
