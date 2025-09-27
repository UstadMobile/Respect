package world.respect.app.view.person.passkeyList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.created_at
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.delete_this_passkey
import world.respect.shared.generated.resources.key_created_on
import world.respect.shared.generated.resources.loss_access_passkey_dialog
import world.respect.shared.util.rememberFormattedDateTime
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
    onClickRevokePasskey: (PersonPasskey) -> Unit
) {
    val passkeyList = uiState.passkeys.dataOrNull() ?: emptyList()

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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = passkeyList,
            key = { it.id }
        ) { personPasskey ->
            val createdAtStr = rememberFormattedDateTime(
                timeInMillis = personPasskey.timeCreated.toEpochMilliseconds(),
                timeZoneId = TimeZone.currentSystemDefault().id,
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = "${stringResource(Res.string.key_created_on)}: ${personPasskey.deviceName}",
                        maxLines = 2,
                    )
                },
                supportingContent = {
                    Text(
                        text = "${stringResource(Res.string.created_at)}: $createdAtStr",
                        maxLines = 1,
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = { onClickRevokePasskey(personPasskey) },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.delete),
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