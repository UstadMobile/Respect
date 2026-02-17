package world.respect.app.view.sharedschooldevice.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Clazz
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        println("DEBUG >> ${lazyPagingItems.itemCount}")

        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.guid ?: index.toString() },
            contentType = { ClassDataSource.ENDPOINT_NAME },
        ) { clazz ->
            println("DEBUG >> ${clazz.toString()}")
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
            OutlinedButton(
                onClick = onClickScanQrCode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Scan QR code badge")
            }
            OutlinedButton(
                onClick = onClickTeacherAdminLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Teacher/admin login")
            }
        }

    }
}
