package world.respect.app.view.manageuser.accountlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_account
import world.respect.shared.generated.resources.logout
import world.respect.shared.generated.resources.profile
import world.respect.shared.generated.resources.settings
import world.respect.shared.viewmodel.manageuser.accountlist.AccountListUiState
import world.respect.shared.viewmodel.manageuser.accountlist.AccountListViewModel

@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    AccountListScreen(
        uiState = uiState,
        onClickAccount = viewModel::onClickAccount,
        onClickAddAccount = viewModel::onClickAddAccount,
        onClickLogout = viewModel::onClickLogout,
        onClickSettings = viewModel::onClickSettings
    )
}

@Composable
fun AccountListScreen(
    uiState: AccountListUiState,
    onClickAccount: (RespectAccount) -> Unit,
    onClickAddAccount: () -> Unit,
    onClickLogout: () -> Unit,
    onClickSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        uiState.selectedAccount?.also { activeAccount ->
            AccountListItem(
                account = activeAccount,
                onClickAccount = null,
                extras = {
                    Row {
                        OutlinedButton(
                            onClick = {},
                        ) {
                            Text(stringResource(Res.string.profile))
                        }

                        Spacer(Modifier.width(16.dp))

                        OutlinedButton(onClick = onClickLogout) {
                            Text(stringResource(Res.string.logout))
                        }
                    }
                }
            )
        }

        HorizontalDivider()

        uiState.accounts.forEach { account ->
            AccountListItem(
                account = account,
                onClickAccount = onClickAccount,
            )
        }

        HorizontalDivider()

        ListItem(
            modifier = Modifier.clickable {
                onClickAddAccount()
            },
            headlineContent = {
                Text(stringResource(Res.string.add_account))
            },
            leadingContent = {
                Icon(Icons.Default.Add, contentDescription = "")
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                onClickSettings()
            },
            headlineContent = {
                Text(stringResource(Res.string.settings))
            },
            leadingContent = {
                Icon(Icons.Default.Settings, contentDescription = "Settings Option")
            }
        )

    }
}