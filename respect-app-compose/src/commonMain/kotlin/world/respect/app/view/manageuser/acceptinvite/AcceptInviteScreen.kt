package world.respect.app.view.manageuser.acceptinvite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDetailField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.class_name
import world.respect.shared.generated.resources.loading
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.role
import world.respect.shared.generated.resources.school_name
import world.respect.shared.generated.resources.school_server_url
import world.respect.shared.util.ext.isLoading
import world.respect.shared.util.ext.label
import world.respect.shared.util.ext.roleLabel
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.manageuser.acceptinvite.AcceptInviteUiState
import world.respect.shared.viewmodel.manageuser.acceptinvite.AcceptInviteViewModel

@Composable
fun AcceptInviteScreen(
    viewModel: AcceptInviteViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    AcceptInviteScreen(
        uiState = uiState,
        appUiState = appUiState,
        onClickNext = viewModel::onClickNext
    )
}

@Composable
fun AcceptInviteScreen(
    uiState: AcceptInviteUiState,
    appUiState: AppUiState,
    onClickNext: () -> Unit
) {
    val invite = uiState.inviteInfo?.invite
    val errorText = uiState.errorText

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            appUiState.isLoading -> {
                Spacer(Modifier.size(16.dp))

                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(Modifier.size(16.dp))

                Text(
                    text =stringResource(Res.string.loading),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            errorText != null -> {
                Spacer(Modifier.size(16.dp))

                Icon(
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(64.dp),
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                )

                Spacer(Modifier.size(16.dp))

                Text(
                    text = uiTextStringResource(errorText),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            invite != null -> {
                when(invite) {
                    is NewUserInvite -> {
                        RespectDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.role)) },
                            value = { Text(stringResource(invite.role.label)) }
                        )
                    }

                    is ClassInvite -> {
                        RespectDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.class_name)) },
                            value = { Text(uiState.inviteInfo?.className ?: "") },
                        )

                        RespectDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.role)) },
                            value = { Text(stringResource(invite.roleLabel)) }
                        )
                    }

                    else -> {
                        //Do nothing else
                    }
                }

                RespectDetailField(
                    modifier = Modifier.defaultItemPadding(),
                    label = { Text(stringResource(Res.string.school_name)) },
                    value = { Text(uiState.schoolName?.getTitle() ?: "") }
                )

                RespectDetailField(
                    modifier = Modifier.defaultItemPadding(),
                    label = { Text(stringResource(Res.string.school_server_url)) },
                    value = { Text(uiState.schoolUrl?.toString() ?: "") }
                )

                Button(
                    onClick = onClickNext,
                    modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                    enabled = uiState.nextButtonEnabled,
                ) {
                    Text(stringResource(Res.string.next))
                }
            }
        }



    }

}
