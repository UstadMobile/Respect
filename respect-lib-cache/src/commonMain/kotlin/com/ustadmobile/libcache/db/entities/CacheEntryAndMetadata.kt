package com.ustadmobile.libcache.db.entities

import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.locks.ReentrantLock

/**
 * Data structure used to hold a CacheEntry and related metadata (locks and extra headers to apply)
 */
data class CacheEntryAndMetadata(
    val urlKey: String,
    val entry: CacheEntry?,
    val extraHeaders: CacheEntryExtraHeaders?,
    val locks: List<RetentionLock>,
    val moveLock: ReentrantLock = ReentrantLock(false),
    val mutex: Mutex = Mutex(),
)

