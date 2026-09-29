package com.ustadmobile.libcache.novarysearch

import io.ktor.http.URLBuilder

/**
 * Normalize the URL being built as per the given NoVarySearch header. This will:
 * a) Remove the search parameters that can be ignored
 * b) Sort the parameters alphabetically if key-order directive is present
 */
fun URLBuilder.normalizeForNoVarySearch(noVarySearch: NoVarySearch): URLBuilder {
    val paramNames = parameters.names()
    paramNames.forEach { name ->
        val isInParams = noVarySearch.params == emptyList<String>() ||
                noVarySearch.params?.contains(name) == true
        val isInExcept = noVarySearch.except?.contains(name) == true

        if(isInParams && !isInExcept) {
            parameters.remove(name)
        }
    }

    if(noVarySearch.keyOrder) {
        val remainingNames = encodedParameters.names()
        val allEncodedValues = remainingNames.map { name ->
            name to encodedParameters.getAll(name)?.sorted()
        }.sortedBy { it.first }

        encodedParameters.clear()
        allEncodedValues.forEach { entry ->
            entry.second?.forEach { paramVal ->
                encodedParameters.append(entry.first, paramVal)
            }
        }
    }

    this.build()
    return this
}