package world.respect.app.app.dateofbirth

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import world.respect.shared.resources.UiText

@Composable
expect fun DateOfBirthSelector(
    date: LocalDate?,
    onDateChanged: (LocalDate) -> Unit,
    label: String,
    error: UiText?
)