package world.respect.app.view.statement.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.langMapString
import world.respect.lib.xapi.ext.objectActivityNameOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.score
import world.respect.shared.util.rememberFormattedDateTime
import kotlin.math.roundToInt

@Composable
fun StatementListItem(
    statement: XapiStatement,
    modifier: Modifier = Modifier,
) {

    val objectName = statement.objectActivityNameOrNull()?.let { langMapString(it) } ?: ""

    val verbName = statement.verb.display?.let { langMapString(it) } ?: ""

    val scoreText = statement.result?.score?.scaled?.let {
        stringResource(Res.string.score) + ":" + "${(it * 100).roundToInt()}%"
    }

    // Placeholder for now
    val iconUrl: String? = null

    val formattedTime = statement.timestamp?.let {
        rememberFormattedDateTime(
            timeInMillis = it.toEpochMilliseconds(),
            timeZoneId = TimeZone.currentSystemDefault().id
        )
    } ?: ""

    ListItem(
        modifier = modifier,
        leadingContent = {
            RespectAsyncImage(
                uri = iconUrl,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(36.dp)
            )
        },
        headlineContent = {
            Text(
                text = objectName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = verbName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (scoreText != null) {
                    Text(
                        text = scoreText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        trailingContent = {
            Text(
                text = formattedTime,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}
