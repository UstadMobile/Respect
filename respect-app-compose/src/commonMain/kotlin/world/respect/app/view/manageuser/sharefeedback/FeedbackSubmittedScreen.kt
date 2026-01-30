package world.respect.app.view.manageuser.sharefeedback

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultScreenPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.feedback_submitted_message
import world.respect.shared.generated.resources.feedback_sent
import world.respect.shared.viewmodel.manageuser.sharefeedback.FeedbackSubmittedViewModel

@Composable
fun FeedbackSubmittedScreen(
    viewModel: FeedbackSubmittedViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultScreenPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item {
            Image(
                painter = painterResource(Res.drawable.feedback_sent),
                contentDescription = null,
                modifier = Modifier.defaultScreenPadding()
                    .size(100.dp, 120.dp)
            )
        }
        item {
            Text(
                modifier = Modifier.defaultItemPadding(),
                text = stringResource(Res.string.feedback_submitted_message),
                textAlign = TextAlign.Center
            )
        }

    }
}