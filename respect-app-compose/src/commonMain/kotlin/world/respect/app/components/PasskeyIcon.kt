package world.respect.app.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun PasskeyIcon(icon:String?) {

    if (icon.isNullOrEmpty()) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
        )
    } else {
        SvgToImage(icon)
    }
}
