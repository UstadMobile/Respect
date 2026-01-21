package world.respect.app.view.sharedschooldevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.empty
import world.respect.shared.generated.resources.last_name
import world.respect.shared.viewmodel.sharedschooldevice.SharedSchoolDeviceEnableViewmodel

@Composable
fun SharedSchoolDeviceEnableScreen(
    viewModel: SharedSchoolDeviceEnableViewmodel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val device = uiState.deviceName

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultItemPadding()
    ) {
        item {
            Text(
                text = "Device name",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.testTag("last_name").fillMaxWidth(),
                value = device,
                label = { Text(stringResource(Res.string.last_name) + "*") },
                onValueChange = { value ->
                    viewModel.updateDeviceName(value)
                },
                singleLine = true,
            )
        }
        item {
            SharedSchoolDeviceInfoBox(
                modifier = Modifier.padding(vertical = 8.dp),
                onClickEnableSharedSchoolDeviceMode = {
                    viewModel.enableSharedDeviceMode()
                }
            )
        }
    }
}

@Composable
fun SharedSchoolDeviceInfoBox(
    onClickEnableSharedSchoolDeviceMode: () -> Unit,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.empty),
                    contentDescription = "",
                    modifier = Modifier
                        .width(120.dp).height(100.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = " Shared device",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "* Student can login without the school name\n" +
                                "* Device auto sync offline to reduce date usage\n" +
                                "* School admin can manually manage",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onClickEnableSharedSchoolDeviceMode,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Enable")
            }
        }
    }
}