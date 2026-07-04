package com.ustadmobile.libcache.novarysearch

/**
 * Represents a NoVarySearch header directives as per:
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/No-Vary-Search
 *
 * This is used to avoid cache misses.
 *
 * @param keyOrder true if present, null if not present, never
 */
class NoVarySearch(
    val keyOrder: Boolean? = null,
    val params: List<String>? = null,
    val except: List<String>? = null,
) {

    companion object {

        fun parse(string: String): NoVarySearch {
            val directives = string.split(",").map { it.trim() }
            val paramsDirective = directives.firstOrNull { it.startsWith("params") }
            val excludesDirective = directives.firstOrNull { it.startsWith("except") }

            return NoVarySearch(
                keyOrder = if(directives.any { it == "key-order" }) true else null,
                params = paramsDirective?.let {
                    if(it == "params") {
                        emptyList()
                    }else {
                        listOf()
                    }
                }
            )
        }
    }

}