package world.respect.app.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.app_version

@Composable
fun RespectShortVersionInfoText(
    modifier: Modifier = Modifier
) {
    val versionInfo = rememberAppVersionInfo()

    versionInfo?.also {
        Text(
            text = "${stringResource(Res.string.app_version)}: ${it.version}",
            modifier = modifier,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }

}