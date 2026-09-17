package com.ustadmobile.libcache.db.ext

import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.CacheEntryAndMetadata

suspend fun UstadCacheDb.getContentEntryAndMetaDataByKey(
    key: String,
    preloadedEntry: CacheEntry?,
) : CacheEntryAndMetadata {
    return CacheEntryAndMetadata(
        urlKey = key,
        entry = preloadedEntry ?: cacheEntryDao.findEntryByKey(key),
        locks = retentionLockDao.findByKey(key),
        extraHeaders = cacheEntryExtraHeadersDao.findByKey(key),
    )
}