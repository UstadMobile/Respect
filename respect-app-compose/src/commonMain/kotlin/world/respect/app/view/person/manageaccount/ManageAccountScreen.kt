package world.respect.app.view.person.manageaccount

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPasskeySignInFasterCard
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.*
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.rememberFormattedDateTime
import world.respect.shared.viewmodel.person.manageaccount.ManageAccountUiState
import world.respect.shared.viewmodel.person.manageaccount.ManageAccountViewModel

@Composable
fun ManageAccountScreen(
    viewModel: ManageAccountViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    ManageAccountScreen(
        uiState = uiState,
        onCreatePasskeyClick = viewModel::onCreatePasskeyClick,
        onClickManagePasskey = viewModel::onClickManagePasskey,
        onClickChangePassword = viewModel::navigateToEditAccount,
        onClickHowPasskeysWork = viewModel::onClickHowPasskeysWork,
    )

}

@Composable
fun ManageAccountScreen(
    uiState: ManageAccountUiState,
    onCreatePasskeyClick: () -> Unit = {},
    onClickHowPasskeysWork: () -> Unit = {},
    onClickManagePasskey: () -> Unit = {},
    onClickChangePassword: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        uiState.errorText?.also {
            Text(
                uiTextStringResource(it),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.defaultItemPadding(),
            )
        }

        ListItem(
            headlineContent = {
                Text(text = uiState.personUsername)
            },
            supportingContent = {
                Text(text = stringResource(Res.string.username_label))
            }
        )

        if (uiState.passkeySupported){
            ListItem(
                headlineContent = {
                    Text(text = stringResource(Res.string.security))
                }
            )


            if (uiState.showCreatePasskey) {
                RespectPasskeySignInFasterCard(
                    modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                    onClickPasskeySignup = onCreatePasskeyClick,
                    onClickHowPasskeysWork = onClickHowPasskeysWork,
                    buttonText = Res.string.create_passkey.asUiText(),
                )
            }

            if (uiState.showManagePasskey) {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickManagePasskey()
                    },
                    leadingContent = {
                        Icon(Icons.Default.Security, contentDescription = null)
                    },
                    headlineContent = {
                        Text(
                            text = "${uiState.passkeyCount ?: ""} ${stringResource(Res.string.passkeys)}",
                            maxLines = 1,
                        )
                    },
                    supportingContent = {
                        Text(text = stringResource(Res.string.passkeys))
                    },
                    trailingContent = {
                        Text(text = stringResource(Res.string.manage))
                    }
                )
            }

        }


        val personPasswordVal = uiState.personPassword.dataOrNull()
        val passwordLastUpdatedStr = rememberFormattedDateTime(
            timeInMillis = personPasswordVal?.lastModified?.toEpochMilliseconds() ?: 0,
            timeZoneId = TimeZone.currentSystemDefault().id,
        )

        ListItem(
            leadingContent = {
                Icon(Icons.Default.Password, contentDescription = null)
            },
            headlineContent = {
                Text(
                    text = stringResource(Res.string.password_label),
                    maxLines = 1,
                )
            },
            supportingContent = {
                if(personPasswordVal != null) {
                    Text(
                        text = "${stringResource(Res.string.last_updated)}: $passwordLastUpdatedStr"
                    )
                }
            },
            trailingContent = {
                Text(
                    modifier = Modifier.clickable { onClickChangePassword() },
                    text = stringResource(Res.string.change_password),
                )
            }
        )


    }
}
