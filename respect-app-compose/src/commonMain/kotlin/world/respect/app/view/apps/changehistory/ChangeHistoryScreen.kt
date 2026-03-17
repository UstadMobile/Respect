package world.respect.app.view.apps.changehistory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.change_format
import world.respect.shared.util.rememberFormattedDateTime
import world.respect.shared.viewmodel.apps.changehistory.ChangeHistoryUiState
import world.respect.shared.viewmodel.apps.changehistory.ChangeHistoryViewModel

@Composable
fun ChangeHistoryScreen(
    viewModel: ChangeHistoryViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    ChangeHistoryScreen(
        uiState = uiState
    )

}

@Composable
fun ChangeHistoryScreen(
    uiState: ChangeHistoryUiState,
) {
    val changeHistoryEntryWithWhoDid = uiState.changeHistoryEntryWithWhoDid ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        changeHistoryEntryWithWhoDid.forEach { group ->

            group.changeHistoryEntry.forEach { entry ->

                entry.changes.forEach { change ->

                    Text(
                        text = group.person.fullName(),
                    )
                    val createdAtStr = rememberFormattedDateTime(
                        timeInMillis = entry.timestamp,
                        timeZoneId = TimeZone.currentSystemDefault().id,
                    )

                    Text(text = createdAtStr)
                    Text(
                        text = stringResource(
                            Res.string.change_format,
                            change.field.displayName,
                            change.oldVal?:"",
                            change.newVal
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                }
            }
        }
    }
}