package org.openeel.libcache.ipc.core

import android.os.Bundle
import okhttp3.Headers

/**
 * To preserve the order of headers (which is not normally needed, but we want to preserve an
 * exact mapping) we need to store a list of header names in the bundle. The bundle itself only
 * has a keySet where the order is undefined.
 */
const val HEADER_NAMES_KEY = "__names__"

/**
 * Convert a Headers object to a Bundle.
 */
fun Headers.toBundle() : Bundle{
    val bundle = Bundle()

    val headerNamesList = Array(size, { "" })

    for(i in 0 until size) {
        val headerName = name(i)
        headerNamesList[i] = headerName

        bundle.putStringArray(headerName, values(headerName).toTypedArray())
    }

    bundle.putStringArray(HEADER_NAMES_KEY, headerNamesList)

    return bundle
}

/**
 * Convert a bundle that was created using Headers.toBundle back to Headers
 *
 * @receiver a Bundle object that was created using Headers.toBundle
 * @return OKHTTP headers object
 */
fun Bundle.toHeaders(): Headers {
    val builder = Headers.Builder()

    val headerNames = getStringArray(HEADER_NAMES_KEY)
        ?: throw IllegalArgumentException("Bundle does not contain header names")

    headerNames.forEach { key ->
        val values = getStringArray(key)
        values?.forEach { value ->
            builder.add(key, value)
        }
    }

    return builder.build()
}
