package world.respect.app.view.clazz.addperson

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import world.respect.shared.generated.resources.copy_invite_code
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.viewmodel.clazz.addperson.AddPersonToClazzUIState
import world.respect.shared.viewmodel.clazz.addperson.AddPersonToClazzViewModel

@Composable
fun AddPersonToClazzScreen(
    viewModel: AddPersonToClazzViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AddPersonToClazzScreen(
        uiState = uiState
    )
}

@Composable
fun AddPersonToClazzScreen(
    uiState: AddPersonToClazzUIState
) {

    Column {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.CopyAll,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            headlineContent = {
                Text(text = stringResource(Res.string.copy_invite_code))
            },
        )
    }
}