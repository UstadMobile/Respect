package world.respect.libutil.ext

/**
 * "Upsert" for a list, commonly required for edit scenarios where an element has been returned
 * that might be updating something already in the list, or it might be a new item.
 * Get the last distinct item in a list by a given key. This could be useful when we have a list of
 * updates, but only want to apply the latest received item.
 */
fun <T> List<T>.replaceOrAppend(
    element: T,
    replacePredicate: (T) -> Boolean,
): List<T> {
    val replaceIndex = indexOfFirst(replacePredicate)
    return if(replaceIndex >= 0) {
        toMutableList().also {
            it[replaceIndex] = element
        }.toList()
    } else {
        (this + element)
    }
}

/**
 * Get the last distinct item in a list by a given key. This could be useful when we have a list of
 * updates, but only want to apply the latest received item.
 */
inline fun <T, K> List<T>.lastDistinctBy(
    selector: (T) -> K
): List<T>{
    val map = mutableMapOf<K, T>()
    forEach {
        map[selector(it)] = it
    }
    return map.values.toList()
}
