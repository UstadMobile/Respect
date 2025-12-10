package world.respect.app.view.shareddevicelogin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Clazz
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.scan_qr_code
import world.respect.shared.generated.resources.teacher_admin_login
import world.respect.shared.viewmodel.sharedschooldevicelogin.SelectClassUiState
import world.respect.shared.viewmodel.sharedschooldevicelogin.SharedSchoolDeviceLoginSelectClassViewModel

@Composable
fun SelectClassScreen(
    viewModel: SharedSchoolDeviceLoginSelectClassViewModel
) {
    val uiState: SelectClassUiState by viewModel.uiState.collectAsState(
        SelectClassUiState()
    )

    SelectClassScreen(
        uiState = uiState,
        onClickClazz = viewModel::onClickClazz,
        onScanQRCode = viewModel::onScanQRCode,
        onTeacherAdminLogin = viewModel::onTeacherAdminLogin
    )
}

@Composable
fun SelectClassScreen(
    uiState: SelectClassUiState,
    onClickClazz: (Clazz) -> Unit,
    onScanQRCode: () -> Unit = {},
    onTeacherAdminLogin: () -> Unit = {}
) {
    val pager = respectRememberPager(uiState.classes)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            respectPagingItems(
                items = lazyPagingItems,
                key = { item, index -> item?.guid ?: index.toString() },
                contentType = { ClassDataSource.ENDPOINT_NAME },
            ) { clazz ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clazz?.also(onClickClazz)
                        },

                    leadingContent = {
                        RespectPersonAvatar(name = clazz?.title ?: "")
                    },

                    headlineContent = {
                        Text(text = clazz?.title ?: "")
                    }
                )
            }
        }

        // Bottom buttons section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Scan QR code button
            Button(
                onClick = onScanQRCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
            ) {
                Text(
                    text = stringResource(Res.string.scan_qr_code),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Teacher/Admin login button
            Button(
                onClick = onTeacherAdminLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
            ) {
                Text(
                    text = stringResource(Res.string.teacher_admin_login),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ClassListItem(
    className: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = className,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Select $className",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}