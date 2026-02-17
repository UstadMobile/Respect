package world.respect.app.view.sharedschooldevice.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import world.respect.shared.viewmodel.sharedschooldevice.login.SelectClassUiState
import world.respect.shared.viewmodel.sharedschooldevice.login.SelectClassViewModel

@Composable
fun SelectClassScreen(
    viewModel: SelectClassViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    SelectClassScreen(
        uiState = uiState,
        onClickClazz = viewModel::onClickClazz,
        onClickScanQrCode = viewModel::onClickScanQrCode,
        onClickTeacherAdminLogin = viewModel::onClickTeacherAdminLogin
    )
}

@Composable
fun SelectClassScreen(
    uiState: SelectClassUiState,
    onClickClazz: (Clazz) -> Unit,
    onClickScanQrCode: () -> Unit,
    onClickTeacherAdminLogin: () -> Unit,
) {
    val pager = respectRememberPager(uiState.classes)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
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
            item {
                Spacer(modifier = Modifier.padding(bottom = 100.dp))
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = onClickScanQrCode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.scan_qr_code))
            }

            OutlinedButton(
                onClick = onClickTeacherAdminLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.teacher_admin_login))
            }
        }
    }
}