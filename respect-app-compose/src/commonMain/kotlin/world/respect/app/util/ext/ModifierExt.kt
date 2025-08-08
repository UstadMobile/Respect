package world.respect.app.util.ext

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * Default padding for Ustad component items: effectively 16dp from the side of the screen, 16dp
 * vertical space between items (8dp top/bottom on each item).
 */
fun Modifier.defaultItemPadding(
    start: Dp = 16.dp,
    top: Dp = 8.dp,
    end: Dp = 16.dp,
    bottom: Dp = 8.dp,
): Modifier = padding(start = start, top = top, end = end, bottom = bottom)