package world.respect.app.view.manageuser.selectAccount

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.app.view.manageuser.accountlist.AccountListItem
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.use_another_account
import world.respect.shared.viewmodel.manageuser.selectaccount.SelectAccountUiState
import world.respect.shared.viewmodel.manageuser.selectaccount.SelectAccountViewModel

@Composable
fun SelectAccountScreen(
    viewModel: SelectAccountViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    SelectAccountScreen(
        uiState = uiState,
        onClickAccount = viewModel::onClickAccount,
        onClickAddAccount = viewModel::onClickAddAccount,
    )
}

@Composable
fun SelectAccountScreen(
    uiState: SelectAccountUiState,
    onClickAccount: (RespectAccount) -> Unit,
    onClickAddAccount: () -> Unit,
) {



    LazyColumn(modifier = Modifier.fillMaxSize()) {


        item("divider1") {
            HorizontalDivider()
        }

        items(
            items = uiState.accounts,
            key = { it.session.account.userGuid }
        ) { account ->
            AccountListItem(
                account = account,
                onClickAccount = onClickAccount,
            )
        }

        item("divider2") {
            HorizontalDivider()
        }

        item("add_account") {
            val text = Res.string.use_another_account
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddAccount()
                },
                headlineContent = {
                    Text(stringResource(text))
                },
                leadingContent = {
                    Icon(Icons.Default.Add, contentDescription = "")
                }
            )
        }

    }
}
