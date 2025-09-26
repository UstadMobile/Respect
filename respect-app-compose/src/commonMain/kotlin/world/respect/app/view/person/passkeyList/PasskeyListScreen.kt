package world.respect.app.view.person.passkeyList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.PersonDataSource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.created
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.delete_this_passkey
import world.respect.shared.generated.resources.loss_access_passkey_dialog
import world.respect.shared.viewmodel.person.passkeylist.PasskeyListUiState
import world.respect.shared.viewmodel.person.passkeylist.PasskeyListViewModel

@Composable
fun PasskeyListScreen(
    viewModel: PasskeyListViewModel
) {

    val uiState by viewModel.uiState.collectAsState()
    PasskeyListScreen(
        viewModel = viewModel,
        uiState = uiState,
        onClickRevokePasskey = viewModel::onClickRevokePasskey

    )
}

@Composable
fun PasskeyListScreen(
    viewModel: PasskeyListViewModel,
    uiState: PasskeyListUiState = PasskeyListUiState(),
    onClickRevokePasskey: (Long) -> Unit

) {
    if (uiState.showRevokePasskeyDialog) {
        showRevokePasskeyDialog(
            onDismissRequest = viewModel::onDismissRevokePasskeyDialog
        ) {
            LazyColumn {
                item {
                    ListItem(

                        headlineContent = { Text(stringResource(Res.string.delete_this_passkey)) },
                        supportingContent = {
                            Text(
                                stringResource(
                                    Res.string.loss_access_passkey_dialog,
                                )
                            )
                        }
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = viewModel::onDismissRevokePasskeyDialog
                        ) {
                            Text(stringResource(Res.string.cancel))
                        }

                        OutlinedButton(
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                                .testTag("delete"),
                            onClick = viewModel::revokePasskey
                        ) {
                            Text(stringResource(Res.string.delete))
                        }
                    }
                }

            }


        }
    }

    val pager = respectRememberPager(uiState.passkeys)

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.personPasskeyUid ?: index.toString() },
            contentType = { PersonDataSource.ENDPOINT_NAME },
        ) { personPasskey ->
            ListItem(
                modifier =
                    Modifier.clickable {
                    },
                headlineContent = {
                    Text(
                        text = "",
                        maxLines = 1,
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "delete"
                    )
                },
                supportingContent = {
                    Text(
                        text = "${stringResource(Res.string.created)}",
                        maxLines = 1,
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = { onClickRevokePasskey(personPasskey?.personPasskeyUid ?: 0) },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "delete"
                        )
                    }

                },
            )


        }
    }


}


@Composable
fun showRevokePasskeyDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        //As per https://developer.android.com/jetpack/compose/components/dialog
        Card(
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            content()
        }
    }
}