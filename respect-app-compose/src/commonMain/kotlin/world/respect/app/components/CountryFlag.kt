package world.respect.app.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.respect.shared.util.getFlagEmoji

@Composable
fun CountryFlag(
    countryCode: String?,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
) {
    val flagEmoji = getFlagEmoji(countryCode)

    val displayEmoji = if (flagEmoji.isEmpty()) "" else flagEmoji

    Text(
        text = displayEmoji,
        fontSize = (size.value * 0.85).sp,
        fontFamily = FontFamily.Default,
        modifier = modifier.size(size)
    )
}
fun Modifier.flagSizeMedium() = this.size(20.dp)
