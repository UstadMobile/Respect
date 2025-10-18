package world.respect.app.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.date
import world.respect.shared.generated.resources.time

@Composable
fun RespectLocalDateTimeField(
    modifier: Modifier = Modifier,
    dateFieldWeight: Float = 0.6f,
    value: LocalDateTime?,
    onValueChanged: (LocalDateTime?) -> Unit,
) {
    var dateVar by remember(value) {
        mutableStateOf(value?.date)
    }

    var timeVar by remember(value) {
        mutableStateOf(value?.time)
    }

    Row(modifier = modifier) {
        RespectLocalDateField(
            modifier = Modifier.weight(dateFieldWeight, true),
            value = value?.date ?: dateVar,
            onValueChange = { newDate ->
                dateVar = newDate
                val timeVal = timeVar
                if(timeVal != null && newDate != null) {
                    onValueChanged(LocalDateTime(newDate, timeVal))
                }else if(newDate == null) {
                    onValueChanged(null)
                }
            },
            label = {
                Text(stringResource(Res.string.date))
            },
        )

        RespectLocalTimeField(
            modifier = Modifier.weight(1f - dateFieldWeight, true),
            value = value?.time ?: timeVar,
            onValueChange = { newTime ->
                timeVar = newTime
                val dateVal = dateVar
                if(dateVal != null && newTime != null) {
                    onValueChanged(LocalDateTime(dateVal, newTime))
                }else if(newTime == null){
                    onValueChanged(null)
                }
            },
            label = {
                Text(stringResource(Res.string.time))
            }
        )
    }
}