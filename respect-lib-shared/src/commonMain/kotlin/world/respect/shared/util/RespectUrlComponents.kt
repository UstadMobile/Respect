package world.respect.shared.util


/**
 * Ustad's URL system is structured as follows:
 *
 * http(s)://server.name[:port]/[subpath/][index.html]#/ViewName?argname=argvalue
 *
 * Production mode:
 *   http(s)://server.name[:port]/[subpath/]#/ViewName?argname1=argvalue1&...
 *
 * Javascript Development Mode:
 *   http://localhost[:port]/#/ViewName?argname1=argvalue1&...
 */
data class RespectUrlComponents(
    val schoolUrl: String,
    val viewName: String,
    val queryString: String,
) {

    val arguments: Map<String, String> by lazy(LazyThreadSafetyMode.NONE) {
        val parsedParams = RespectFileUtil.parseParams(queryString, '&')

        parsedParams.map {
            RespectURLEncoder.decodeUTF8(it.key) to RespectURLEncoder.decodeUTF8(it.value)
        }.toMap()
    }

    val viewUri: String by lazy {
        if(queryString.isEmpty()) {
            viewName
        }else {
            "$viewName?$queryString"
        }
    }

    fun fullUrl(): String {
        return RespectFileUtil.joinPaths(schoolUrl, viewUri)
    }

    companion object {

        const val DEFAULT_DIVIDER = "/#/"

        fun parse(url: String, divider: String = DEFAULT_DIVIDER) : RespectUrlComponents {
            val dividerIndex = url.indexOf(divider)
            if(dividerIndex == -1)
                throw IllegalArgumentException("Not a valid UstadUrl: $url")

            //Endpoint should include the trailing /
            val endpoint = url.substring(0, dividerIndex + 1)
            val queryIndex = url.indexOf("?", startIndex = dividerIndex)
            val viewName: String
            val queryString: String
            if(queryIndex == -1 || queryIndex == (url.length -1)) {
                viewName = url.substring(dividerIndex + divider.length).removeSuffix("?")
                queryString = ""
            }else {
                viewName = url.substring(dividerIndex + divider.length, queryIndex)
                queryString = url.substring(queryIndex + 1)
            }

            return RespectUrlComponents(endpoint, viewName, queryString)
        }
    }

}