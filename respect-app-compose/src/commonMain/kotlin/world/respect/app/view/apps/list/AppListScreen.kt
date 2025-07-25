package world.respect.app.view.apps.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_from_link
import world.respect.app.app.RespectAsyncImage
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.apps.list.AppListUiState
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel.Companion.EMPTY_LIST
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.dataOrNull
import world.respect.libutil.ext.resolve

@Composable
fun AppListScreen(
    viewModel: AppListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    AppListScreen(
        uiState = uiState,
        onClickAddLink = { viewModel.onClickAddLink() },
        onClickApp = { viewModel.onClickApp(it) }
    )
}

@Composable
fun AppListScreen(
    uiState: AppListUiState,
    onClickAddLink: () -> Unit,
    onClickApp: (DataLoadState<RespectAppManifest>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = EMPTY_LIST) {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickAddLink() },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.add_from_link),
                    )
                }
            )
        }

        itemsIndexed(
            items = uiState.appList,
            key = { index, app ->
                app.metaInfo.url?.toString() ?: index
            }
        ) { index, app ->
            val appData = app.dataOrNull()
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClickApp(app)
                    },
                leadingContent = {
                    appData?.icon.also { icon ->
                        val appIcon = app.metaInfo.url?.resolve(
                            icon.toString()
                        ).toString()

                        RespectAsyncImage(
                            uri = appIcon,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                        )
                    }
                },
                headlineContent = {
                    Text(
                        text = appData?.name?.getTitle() ?: "",
                    )
                },
                supportingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        //"-" is a placeholder for age range/category
                        Text("-")
                        Text("-")
                    }
                },

                )
        }
    }
}
