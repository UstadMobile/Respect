package com.ustadmobile.libcache.novarysearch

/**
 * Represents a NoVarySearch header directives as per:
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/No-Vary-Search
 *
 * This is used to avoid cache misses.
 *
 * @param keyOrder true if present, false otherwise
 * @param params if there are specified names in the params directive eg params=("id", "lang") then
 *        those specified names. The params directive is on its own (e.g. covers all parameters),
 *        then an empty list. If the directive is not included, then null
 * @param except a list of specified names in the except directive if present, otherwise null
 */
data class NoVarySearch(
    val keyOrder: Boolean = false,
    val params: List<String>? = null,
    val except: List<String>? = null,
) {

    companion object {

        private val whiteSpaceRegex = Regex("\\s+")

        private fun parseDirective(directiveVal: String) : List<String>{
            val directiveName = directiveVal.substringBefore(
                "=", directiveVal
            )

            return if(directiveVal == directiveName) {
                emptyList()
            }else {
                val paramsSection = directiveVal.substringAfter("(")
                    .substringBefore(")")

                paramsSection.split(whiteSpaceRegex).map {
                    it.removeSurrounding("\"")
                }
            }
        }


        fun parse(string: String): NoVarySearch {
            /*
             * http header names cannot contain delimiters and the header names in params and except
             * are separated by spaces.
             */
            val directives = string.split(",").map { it.trim() }

            return NoVarySearch(
                keyOrder = directives.any { it == "key-order" },
                params = directives.firstOrNull { it.startsWith("params") }?.let {
                    parseDirective(it)
                },
                except = directives.firstOrNull { it.startsWith("except") }?.let {
                    parseDirective(it)
                }
            )
        }
    }

}