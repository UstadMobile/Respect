package world.respect.app.view.person.manageaccount

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectBottomSheetOption
import world.respect.app.components.RespectPasskeySignInFasterCard
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.app.view.person.setusernameandpassword.QrCodeInfoBox
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign_new_badge_replace
import world.respect.shared.generated.resources.change
import world.respect.shared.generated.resources.create_passkey
import world.respect.shared.generated.resources.last_updated
import world.respect.shared.generated.resources.manage
import world.respect.shared.generated.resources.passkeys
import world.respect.shared.generated.resources.password_label
import world.respect.shared.generated.resources.qr_code_badge
import world.respect.shared.generated.resources.remove_badge
import world.respect.shared.generated.resources.security
import world.respect.shared.generated.resources.username_label
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.rememberFormattedDateTime
import world.respect.shared.viewmodel.person.manageaccount.ManageAccountUiState
import world.respect.shared.viewmodel.person.manageaccount.ManageAccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccountScreen(
    viewModel: ManageAccountViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showBottomSheet) {
        RespectQRBadgeOptionsBottomSheet(
            onAssignNewBadge = {
                viewModel.onClickQRCodeBadge()
            },
            onRemoveBadge = {
                viewModel.onRemoveQRBadge()
            },
            onDismissRequest = { viewModel.onDismissBottomSheet() }
        )
    }
    ManageAccountScreen(
        uiState = uiState,
        onCreatePasskeyClick = viewModel::onCreatePasskeyClick,
        onClickManagePasskey = viewModel::onClickManagePasskey,
        onClickChangePassword = viewModel::onClickChangePassword,
        onClickHowPasskeysWork = viewModel::onClickHowPasskeysWork,
        onAssignQrCodeBadge = viewModel::onClickQRCodeBadge,
        onClickChangeQrBadge = viewModel::onClickChangeQrBadge,
    )

}

@Composable
fun ManageAccountScreen(
    uiState: ManageAccountUiState,
    onCreatePasskeyClick: () -> Unit = {},
    onClickHowPasskeysWork: () -> Unit = {},
    onClickManagePasskey: () -> Unit = {},
    onClickChangePassword: () -> Unit = {},
    onAssignQrCodeBadge: () -> Unit = {},
    onLearnMore: () -> Unit = {},
    onClickChangeQrBadge: () -> Unit = {},
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

        if (uiState.passkeySupported || uiState.isStudent) {
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
        if (uiState.isStudent) {
            val personQrVal = uiState.qrBadge.dataOrNull()
            val qrLastUpdatedStr = rememberFormattedDateTime(
                timeInMillis = personQrVal?.lastModified?.toEpochMilliseconds() ?: 0,
                timeZoneId = TimeZone.currentSystemDefault().id,
            )

            if (personQrVal != null) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.qr_code_badge),
                            maxLines = 1,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "${stringResource(Res.string.last_updated)}: $qrLastUpdatedStr"
                        )
                    },
                    trailingContent = {
                        OutlinedButton(
                            onClick = {
                                onClickChangeQrBadge()
                            }
                        ) {
                            Text(
                                text = stringResource(Res.string.change),
                            )
                        }
                    }
                )
            } else {
                QrCodeInfoBox(
                    onLearnMore,
                    onAssignQrCodeBadge,
                    modifier = Modifier
                        .fillMaxWidth()
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
                if (personPasswordVal != null) {
                    Text(
                        text = "${stringResource(Res.string.last_updated)}: $passwordLastUpdatedStr"
                    )
                }
            },
            trailingContent = {
                OutlinedButton(
                    onClick = {
                        onClickChangePassword()
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.change),
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespectQRBadgeOptionsBottomSheet(
    onAssignNewBadge: () -> Unit = {},
    onRemoveBadge: () -> Unit = {},
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            Modifier.verticalScroll(
                state = rememberScrollState()
            ).fillMaxSize()
        ) {
            RespectBottomSheetOption(
                modifier = Modifier.clickable {
                    onDismissRequest()
                    onAssignNewBadge()
                },
                headlineContent = {
                    Text(stringResource(Res.string.assign_new_badge_replace))
                },
            )
            RespectBottomSheetOption(
                modifier = Modifier.clickable {
                    onDismissRequest()
                    onRemoveBadge()
                },
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.remove_badge),
                        color = MaterialTheme.colorScheme.error
                    )
                },
            )
        }
    }
}