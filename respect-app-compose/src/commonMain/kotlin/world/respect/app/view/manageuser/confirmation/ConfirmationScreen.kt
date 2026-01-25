package world.respect.app.view.manageuser.confirmation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDetailField
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.role
import world.respect.shared.generated.resources.school_name
import world.respect.shared.generated.resources.school_server_url
import world.respect.shared.util.ext.label
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.manageuser.confirmation.ConfirmationUiState
import world.respect.shared.viewmodel.manageuser.confirmation.ConfirmationViewModel

@Composable
fun ConfirmationScreen(
    viewModel: ConfirmationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    ConfirmationScreen(
        uiState = uiState,
        onClickStudent = viewModel::onClickStudent,
        onClickParent = viewModel::onClickParent,
        onClickNext = viewModel::onClickNext
    )
}

@Composable
fun ConfirmationScreen(
    uiState: ConfirmationUiState,
    onClickStudent: () -> Unit,
    onClickParent: () -> Unit,
    onClickNext: () -> Unit
) {
    val invite = uiState.inviteInfo?.invite

    Column(modifier = Modifier.fillMaxSize()) {
        when(invite) {
            is NewUserInvite -> {
                RespectDetailField(
                    modifier = Modifier.defaultItemPadding(),
                    label = { Text(stringResource(Res.string.role)) },
                    value = { Text(stringResource(invite.role.label)) }
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
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        ) {
            Text(stringResource(Res.string.next))
        }
    }

}
