package world.respect.lib.opds.model


/**
 * Converts a [LangMap] instance to a standard [Map] of [String] to [String].
 *
 * If the [LangMap] is a [LangMapStringValue], it returns a map with an empty string key
 * and the `value` of the [LangMapStringValue] as its value.
 * If the [LangMap] is a [LangMapObjectValue], it returns the underlying `map` property.
 *
 * @return A [Map<String, String>] representation of the [LangMap].
 */
fun LangMap.toStringMap(): Map<String, String> {
    return when(this) {
        is LangMapStringValue -> mapOf("" to this.value)
        is LangMapObjectValue -> this.map
    }
}