package world.respect.app.view.manageuser.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultScreenPadding
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.i_am_parent
import world.respect.shared.generated.resources.i_am_student
import world.respect.shared.generated.resources.invitation_for
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.successfully_registered_school
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {
        /*
        Text(
            text = if (uiState.inviteInfo?.invite?.firstUser == true)
                stringResource(Res.string.successfully_registered_school)
            else
                stringResource(Res.string.invitation_for),
            Modifier.defaultItemPadding()
        )
         */

        ListItem(
            headlineContent = {
                Text(text = uiState.inviteInfo?.className ?: "")
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = uiState.inviteInfo?.classGuid ?: "")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))


        /*
        if (!uiState.isTeacherInvite&&
            uiState.inviteInfo?.invite?.newRole== PersonRoleEnum.STUDENT&&
            uiState.inviteInfo?.invite?.forFamilyOfGuid==null) {

            OutlinedButton(
                onClick = onClickStudent,
                modifier = Modifier.fillMaxWidth().defaultItemPadding()

            ) {
                Text(stringResource(Res.string.i_am_student))
            }

            OutlinedButton(
                onClick = onClickParent,
                modifier = Modifier.fillMaxWidth().defaultItemPadding()

            ) {
                Text(stringResource(Res.string.i_am_parent))
            }
        }
        if (uiState.inviteInfo?.invite?.newRole!= PersonRoleEnum.STUDENT||
            uiState.inviteInfo?.invite?.forFamilyOfGuid!=null) {
            Button(
                onClick = onClickNext,
                modifier = Modifier.fillMaxWidth().defaultItemPadding()
            ) {
                Text(stringResource(Res.string.next))
            }
        }

         */
    }

}
