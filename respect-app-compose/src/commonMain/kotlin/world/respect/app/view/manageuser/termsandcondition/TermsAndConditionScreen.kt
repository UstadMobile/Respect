package world.respect.app.view.manageuser.termsandcondition

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultScreenPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.accept
import world.respect.shared.viewmodel.manageuser.termsandcondition.TermsAndConditionUiState
import world.respect.shared.viewmodel.manageuser.termsandcondition.TermsAndConditionViewModel

@Composable
fun TermsAndConditionScreen(viewModel: TermsAndConditionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    TermsAndConditionScreen(
        uiState = uiState,
        onAcceptClicked = viewModel::onAcceptClicked
    )
}

@Composable
fun TermsAndConditionScreen(
    uiState: TermsAndConditionUiState,
    onAcceptClicked: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = uiState.termsAndConditionText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAcceptClicked,
            modifier = Modifier
                .defaultItemPadding()
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(stringResource(Res.string.accept))
        }
    }

}
