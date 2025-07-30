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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.stringResource
import world.respect.app.util.ext.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.indicator_name
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.sql
import world.respect.shared.viewmodel.report.indictor.IndictorEditViewmodel

@Composable
fun IndictorEditScreen(
    navController: NavHostController,
    viewModel: IndictorEditViewmodel
) {
    //TODO
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "",
                label = { Text(stringResource(Res.string.indicator_name) + "*") },
                singleLine = true,
                onValueChange = {},
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                supportingText = {
                }
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "",
                label = { Text(stringResource(Res.string.description) + "*") },
                singleLine = true,
                onValueChange = {},
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                supportingText = {
                }
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp), // Set min and max height
                value = "",
                label = { Text(stringResource(Res.string.sql) + "*") },
                onValueChange = {},
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace // Use monospace font for SQL
                )
            )
        }
    }
}