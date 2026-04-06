package world.respect.datalayer.school.xapi.ext

fun <K: Any, V: Any> Map<K, V>.takeIfNotEmpty() = this.ifEmpty { null }

