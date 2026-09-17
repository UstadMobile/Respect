package com.ustadmobile.libcache.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.ktor.http.Url

/**
 * Represents a cache entry.
 *
 * @param key The url key is the md5 of the url (base64 encoded). This creates a unique key based on
 *        the URL, and improves performance when searching/indexing because it is shorter than the
 *        url itself. URLs by nature often have matching prefixes (which slows down searching through
 *        them as an equality check has to go further through a non-matching string before it can
 *        return false).
 * @param url the URL. If the response contained the no-vary-search header, the search parameters
 *        will be normalized as per Url.normalizeForNoVarySearch .
 * @param urlWithoutSearch the URL without any search parameters, non-null if the NoVarySearch header
 *        is set.
 *
 * @param urlWithoutSearchHash the MD5 of the URL without any search parameters, non-null if the NoVarySearch header
 *        is set.
 * @param cacheFlags flags from the cache-control header.
 * @param storageSize the size of the entry as it is stored on the disk. If the entry stored gzipped,
 *        this is the size after compression.
 */
@Entity(
    indices = [
        Index("lastAccessed", name = "idx_lastAccessed"),
        Index("urlWithoutSearchHash", name = "idx_urlWithoutSearchHash")
    ],
)
data class CacheEntry(
    @PrimaryKey
    val key: String = "",

    val url: Url,

    val urlWithoutSearch: Url? = null,

    val urlWithoutSearchHash: String? = null,

    val message: String = "",

    val statusCode: Int = 0,

    val cacheFlags: Int = 0,

    val method: Int = 0,

    val lastAccessed: Long = 0,

    val lastValidated: Long = -1,

    @ColumnInfo(index = true)
    val integrity: String? = null,

    val responseHeaders: String = "",

    /**
     * The path where the body of the request is stored as kotlinx.io.Path.toString
     */
    val storageUri: String = "",

    val storageSize: Long = 0,

    /**
     * The size of the entry, uncompressed. This might be different to storageSize if the entry is
     * stored using content-encoding Gzip etc.
     */
    @ColumnInfo(defaultValue = "0")
    val uncompressedSize: Long = 0,
)
