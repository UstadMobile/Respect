package world.respect.app.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T> RespectSortHeader(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelContent: @Composable (() -> Unit)? = null,
    optionLabel: @Composable ((T) -> String),
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (labelContent != null) {
                    labelContent()
                } else {
                    Text(text = optionLabel(selectedOption), fontSize = 14.sp)
                }
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    modifier = Modifier.padding(6.dp),
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(text = optionLabel(option), fontSize = 14.sp)
                    },
                    onClick = {
                        expanded = false
                        onOptionSelected(option as String)
                    }
                )
            }
        }
    }
}
