package world.respect.app.view.person.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDetailField
import world.respect.app.components.RespectQuickActionButton
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.viewmodel.person.detail.PersonDetailUiState
import world.respect.shared.viewmodel.person.detail.PersonDetailViewModel
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.date_of_birth
import world.respect.shared.generated.resources.email
import world.respect.shared.generated.resources.gender
import world.respect.shared.generated.resources.phone_memory
import world.respect.shared.generated.resources.phone_number
import world.respect.shared.generated.resources.username_label
import world.respect.shared.generated.resources.manage_account
import world.respect.shared.util.ext.label

@Composable
fun PersonDetailScreen(
    viewModel: PersonDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    PersonDetailScreen(
        uiState = uiState,
        onClickManageAccount = { viewModel.navigateToManageAccount() }
    )
}

@Composable
fun PersonDetailScreen(
    uiState: PersonDetailUiState,
    onClickManageAccount:()->Unit
) {
    val person = uiState.person.dataOrNull()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (uiState.manageAccountVisible){
                RespectQuickActionButton(
                    labelText = stringResource(Res.string.manage_account),
                    imageVector = Icons.Default.Key,
                    onClick = onClickManageAccount
                )
            }
        }

        HorizontalDivider()

        person?.username?.also {
            RespectDetailField(
                modifier = Modifier.defaultItemPadding(),
                label = { Text(stringResource(Res.string.username_label)) },
                value = { Text(it) }
            )
        }

        RespectDetailField(
            modifier = Modifier.defaultItemPadding(),
            label = { Text(stringResource(Res.string.gender)) },
            value = { Text(uiState.person.dataOrNull()?.gender?.label?.let { stringResource(it) } ?: "")}
        )

        person?.dateOfBirth?.also {
            RespectDetailField(
                modifier = Modifier.defaultItemPadding(),
                label = { (Text(stringResource(Res.string.date_of_birth))) },
                value = { Text(it.toString()) }
            )
        }
        person?.phoneNumber?.also {
            RespectDetailField(
                modifier = Modifier.defaultItemPadding(),
                label = { Text(stringResource(Res.string.phone_number)) },
                value = { Text(it) }
            )
        }
        person?.email?.also {
            RespectDetailField(
                modifier = Modifier.defaultItemPadding(),
                label = { Text(stringResource(Res.string.email)) },
                value = { Text(it) }
            )
        }
    }
}
