package world.respect.app.view.sharedschooldevice.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import world.respect.app.components.RespectPersonAvatar
import world.respect.shared.viewmodel.sharedschooldevice.login.SelectClassViewmodel

@Composable
fun SelectClassScreen(
    viewModel: SelectClassViewmodel,
) {
    val uiState by viewModel.uiState.collectAsState()
    Column {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().testTag("schools_list")
        ) {
            items(
                count = uiState.clazz.size,
                key = { index -> uiState.clazz[index].toString() }
            ) { index ->
                val clazz = uiState.clazz[index]

                ListItem(
                    leadingContent = {
                        RespectPersonAvatar(name = clazz.title)
                    },
                    headlineContent = {
                        Column {
                            Text(
                                text = clazz.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    tonalElevation = 0.dp
                )
            }
            item {

            }
        }
        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Scan QR code badge")
        }
        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Teacher/admin login")
        }
    }
}
