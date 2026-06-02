package world.respect.app.components

import androidx.compose.runtime.Composable
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.LangMapObjectValue
import world.respect.lib.opds.model.LangMapStringValue

/**
 * This will be more sophisticated in future (will select the appropriate language), for now, it will
 * just get the first answer available.
 */
@Composable
fun langMapString(langMap: LangMap): String {
    return when(langMap) {
        is LangMapStringValue -> langMap.value
        is LangMapObjectValue -> langMap.map.values.firstOrNull() ?: ""
    }
}

@Composable
fun langMapString(langMap: Map<String, String>) : String {
    return langMap.values.firstOrNull() ?: ""
}
