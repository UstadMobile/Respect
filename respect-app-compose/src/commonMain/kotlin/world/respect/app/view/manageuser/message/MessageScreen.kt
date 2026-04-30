package world.respect.app.view.manageuser.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.link_is
import world.respect.shared.viewmodel.manageuser.message.MessageUiState
import world.respect.shared.viewmodel.manageuser.message.MessageViewModel

@Composable
fun MessageScreen(
    viewModel: MessageViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    MessageScreen(
        uiState = uiState,
        onClickLink = viewModel::onClickLink,
    )
}

@Composable
fun MessageScreen(
    uiState: MessageUiState,
    onClickLink: () -> Unit,
) {


    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item("link") {
            ListItem(

                modifier = Modifier.clickable {
                    onClickLink()
                },
                headlineContent = {
                    Text(stringResource(Res.string.link_is))
                },
                supportingContent = {
                    Text(text = uiState.link ?: "")
                }
            )
        }
    }
}
