package world.respect.app.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.http.Url
import org.koin.compose.koinInject
import world.respect.shared.domain.country.GetCountryForUrlUseCase
import world.respect.shared.util.getFlagEmoji

@Composable
fun CountryFlag(
    schoolUrl: Url,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    getCountryForUrlUseCase: GetCountryForUrlUseCase = koinInject(),
) {
    var countryCode by remember(schoolUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(schoolUrl) {
        countryCode = try {
            getCountryForUrlUseCase(schoolUrl)
        } catch (e: Exception) {
            null
        }
    }
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
