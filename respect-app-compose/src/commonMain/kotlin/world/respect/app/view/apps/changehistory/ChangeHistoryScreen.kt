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
import world.respect.datalayer.school.model.ChangeHistoryFieldEnum
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
                        timeInMillis = entry.lastModified.toEpochMilliseconds(),
                        timeZoneId = TimeZone.currentSystemDefault().id,
                    )

                    Text(text = createdAtStr)
                    val text = when (change.field) {

                        ChangeHistoryFieldEnum.JOIN_REQUEST_APPROVED ->
                            "${change.field.displayName} \"${change.newVal}\""

                        ChangeHistoryFieldEnum.JOIN_REQUEST_REJECTED ->
                            "${change.field.displayName} \"${change.oldVal}\""

                        ChangeHistoryFieldEnum.CLASS_TEACHER_ADDED,
                        ChangeHistoryFieldEnum.CLASS_STUDENT_ADDED ->
                            "${change.field.displayName}: \"${change.newVal}\""

                        ChangeHistoryFieldEnum.CLASS_TEACHER_REMOVED,
                        ChangeHistoryFieldEnum.CLASS_STUDENT_REMOVED ->
                            "${change.field.displayName}: \"${change.newVal}\""

                        else ->
                            stringResource(
                                Res.string.change_format,
                                change.field.displayName,
                                change.oldVal ?: "",
                                change.newVal
                            )
                    }

                    Text(text = text)
                    Spacer(modifier = Modifier.height(16.dp))

                }
            }
        }
    }
}