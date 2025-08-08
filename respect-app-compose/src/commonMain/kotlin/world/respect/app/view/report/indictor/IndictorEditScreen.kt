package world.respect.app.view.report.indictor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.util.ext.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.field
import world.respect.shared.generated.resources.sql
import world.respect.shared.viewmodel.report.indictor.IndicatorEditUiState
import world.respect.shared.viewmodel.report.indictor.IndicatorEditViewModel

@Composable
fun IndictorEditScreen(
    navController: NavHostController,
    viewModel: IndicatorEditViewModel
) {
    val uiState: IndicatorEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = IndicatorEditUiState(),
        context = Dispatchers.Main.immediate
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.indicatorData.name ?: "",
                label = { Text(stringResource(Res.string.field) + "*") },
                singleLine = true,
                onValueChange = { newName ->
                    viewModel.updateIndicator { it.copy(name = newName) }
                },
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.indicatorData.description ?: "",
                label = { Text(stringResource(Res.string.description) + "*") },
                singleLine = true,
                onValueChange = { newDesc ->
                    viewModel.updateIndicator { it.copy(description = newDesc) }
                },
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp),
                value = uiState.indicatorData.sql ?: "",
                label = { Text(stringResource(Res.string.sql) + "*") },
                onValueChange = { newSql ->
                    viewModel.updateIndicator { it.copy(sql = newSql) }
                },
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}