package world.respect.app.view.person.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDetailField
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.ext.dataOrNull
import world.respect.shared.viewmodel.person.detail.PersonDetailUiState
import world.respect.shared.viewmodel.person.detail.PersonDetailViewModel
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.date_of_birth
import world.respect.shared.generated.resources.gender
import world.respect.shared.generated.resources.username_label
import world.respect.shared.util.ext.label

@Composable
fun PersonDetailScreen(
    viewModel: PersonDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    PersonDetailScreen(uiState)
}

@Composable
fun PersonDetailScreen(
    uiState: PersonDetailUiState,
) {
    val person = uiState.person.dataOrNull()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        person?.username?.also {
            RespectDetailField(
                modifier = Modifier.defaultItemPadding(),
                label = { Text(stringResource(Res.string.username_label)) },
                value = { Text(it) }
            )
        }

        RespectDetailField(
            modifier = Modifier.defaultItemPadding(),
            label = { Text(stringResource(Res.string.gender)) },
            value = { Text(uiState.person.dataOrNull()?.gender?.label?.let { stringResource(it) } ?: "")}
        )

        person?.dateOfBirth?.also {
            RespectDetailField(
                modifier = Modifier.defaultItemPadding(),
                label = { (Text(stringResource(Res.string.date_of_birth))) },
                value = { Text(it.toString()) }
            )
        }

    }
}
