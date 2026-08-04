package world.respect.app.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import org.koin.compose.koinInject
import world.respect.shared.domain.country.GetCountryForUrlUseCase
import world.respect.shared.util.getFlagEmoji

/**
 * Shows the flag of the country in which the given school is hosted. Shows nothing where the
 * country cannot be determined.
 */
@Composable
fun CountryFlag(
    schoolUrl: Url,
    modifier: Modifier = Modifier,
    getCountryForUrlUseCase: GetCountryForUrlUseCase = koinInject(),
) {
    var countryCode by remember(schoolUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(schoolUrl) {
        countryCode = try {
            getCountryForUrlUseCase(schoolUrl)
        } catch (e: Exception) {
            Napier.w("CountryFlag: could not get country for $schoolUrl", e)
            null
        }
    }

    getFlagEmoji(countryCode).takeIf { it.isNotEmpty() }?.also { flagEmoji ->
        Text(
            text = flagEmoji,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier,
        )
    }
}