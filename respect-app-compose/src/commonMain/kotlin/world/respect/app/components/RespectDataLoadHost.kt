package world.respect.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.shared.util.exception.getUiTextOrGeneric

/**
 * Simple component that will normally pass simply pass through the contents. If the data load
 * state is an error, it will NOT display the contents and will instead show the an error message
 *
 * It essentially functions similarly to a guard route.
 *
 * @param dataLoadState The data load state as above.
 */
@Composable
fun RespectDataLoadHost(
    dataLoadState: DataLoadState<*>,
    modifier: Modifier = Modifier,
    contents: @Composable () -> Unit
) {
    if(dataLoadState is DataErrorResult<*>) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
            )

            Text(
                text = uiTextStringResource(dataLoadState.error.getUiTextOrGeneric())
            )
        }
    }else {
        contents()
    }
}