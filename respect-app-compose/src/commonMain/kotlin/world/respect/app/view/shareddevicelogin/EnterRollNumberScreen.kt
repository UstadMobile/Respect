package world.respect.app.view.shareddevicelogin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_roll_number
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.required
import world.respect.shared.viewmodel.sharedschooldevicelogin.EnterRollNumberUiState
import world.respect.shared.viewmodel.sharedschooldevicelogin.EnterRollNumberViewModel


@Composable
fun EnterRollNumberScreen(
    viewModel: EnterRollNumberViewModel
) {
    val uiState: EnterRollNumberUiState by viewModel.uiState.collectAsState(
        EnterRollNumberUiState()
    )
    EnterRollNumberScreen(
        uiState = uiState,
        onRollNumberChange = viewModel::onRollNumberChange,
        onNextClick = viewModel::onNextClick,
        isValid = uiState.isValid
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterRollNumberScreen(
    uiState: EnterRollNumberUiState,
    onRollNumberChange: (String) -> Unit,
    onNextClick: () -> Unit,
    isValid: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        OutlinedTextField(
            value = uiState.rollNumber,
            onValueChange = onRollNumberChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ),
            label = {
                Text(
                    text = stringResource(Res.string.enter_roll_number),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            shape = RoundedCornerShape(12.dp)
        )
        Text(
            text = stringResource(Res.string.required),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
        ) {
            Text(
                text = stringResource(Res.string.next),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}