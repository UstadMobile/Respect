package com.ustadmobile.libcache

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ustadmobile.ihttp.headers.IHttpHeader
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.headers.requireIntegrity
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.RangeInputStream
import com.ustadmobile.libcache.io.uncompress
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.downloader.EnqueuePinPublicationPrepareUseCaseJvm
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.response.StringResponse
import com.ustadmobile.libcache.response.bodyAsUncompressedSourceIfContentEncoded
import com.ustadmobile.libcache.util.initNapierLog
import com.ustadmobile.libcache.util.newFileFromResource
import com.ustadmobile.libcache.util.storeFileAsUrl
import kotlinx.coroutines.runBlocking
import kotlinx.io.asInputStream
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.ByteArrayInputStream
import java.io.File
import java.io.SequenceInputStream
import java.security.MessageDigest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class UstadCacheJvmTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private lateinit var rootDir: File

    private lateinit var temporaryFolderPathsProvider: CachePathsProvider

    private lateinit var cachePaths: CachePaths

    @BeforeTest
    fun setup(){
        rootDir = tempDir.newFolder()
        initNapierLog()
        val rootPath = Path(rootDir.absolutePath)
        cachePaths = CachePaths(
            tmpWorkPath = Path(rootPath, "tmpWork"),
            persistentPath = Path(rootPath, "persistent"),
            cachePath = Path(rootPath, "cache")
        )
        temporaryFolderPathsProvider = CachePathsProvider {
            cachePaths
        }
    }

    private fun UstadCache.assertCanStoreAndRetrieveFileAsCacheHit(
        testFile: File,
        testUrl: String,
        mimeType: String,
        expectedContentEncoding: String? = null,
        requestHeaders: List<IHttpHeader> = emptyList(),
    ) {
        val request = runBlocking {
            storeFileAsUrl(
                testFile = testFile,
                testUrl = testUrl,
                mimeType = mimeType,
                requestHeaders = requestHeaders
            ).request
        }


        //Check response body content matches
        val cacheResponse = runBlocking { retrieve(request) }
        assertNotNull(cacheResponse, "cache response for $testUrl is not null")
        val bodyBytesRaw = cacheResponse.bodyAsSource()!!.readByteArray()

        val bodyBytesDecoded = ByteArrayInputStream(bodyBytesRaw).uncompress(
            CompressionType.byHeaderVal(cacheResponse.headers["content-encoding"])
        ).readAllBytes()

        Assert.assertArrayEquals(testFile.readBytes(), bodyBytesDecoded)

        val dataSha256 = MessageDigest.getInstance("SHA-256").also {
            it.update(testFile.readBytes())
        }.digest()

        val integrityHeaderVal = cacheResponse.headers.requireIntegrity()
        Assert.assertEquals(sha256Integrity(dataSha256), integrityHeaderVal)

        //If content-encoding was set, then content-length will not be the same as input file
        assertEquals(bodyBytesRaw.size.toLong(), cacheResponse.headers["content-length"]?.toLong())
        assertEquals(mimeType, cacheResponse.headers["content-type"])

        if(expectedContentEncoding != null) {
            val numEncodingHeaders = cacheResponse.headers.getAllByName("content-encoding").size
            if(expectedContentEncoding == "identity") {
                assertTrue(cacheResponse.headers["content-encoding"].let { it == null || it == "identity" },
                    "Content-encoding for $testUrl should be identity - can have no header, or can be set to identity")
                assertTrue(numEncodingHeaders == 1 || numEncodingHeaders == 0)
            }else {
                assertEquals(expectedContentEncoding, cacheResponse.headers["content-encoding"],
                    "Content-encoding for $testUrl should be $expectedContentEncoding")
                assertEquals(1, numEncodingHeaders)
            }
        }

    }

    data class FileCanBeCachedAndRetrievedContext(
        val cacheDb: UstadCacheDb,
        val cache: UstadCacheImpl,
        val createdLocks: List<Pair<EntryLockRequest, RetentionLock>>,
    )

    private fun assertFileCanBeCachedAndRetrieved(
        testFile: File,
        testUrl: String,
        mimeType: String,
        expectContentEncoding: String? = null,
        requestHeaders: List<IHttpHeader> = emptyList(),
        createLock: Boolean = false,
        block: FileCanBeCachedAndRetrievedContext.() -> Unit = { },
    ) {
        val tmpFile = tempDir.newFile("cachetest.db")
        val cacheDb = Room.databaseBuilder<UstadCacheDb>(tmpFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()
        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            db = cacheDb,
            xxStringHasher = XXStringHasherCommonJvm(),
            logger = NapierLoggingAdapter(),
            enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                cacheDb, XXStringHasherCommonJvm()
            ),
        )

        val createdLocks = if(createLock) {
            runBlocking { ustadCache.addRetentionLocks(listOf(EntryLockRequest(testUrl))) }
        }else {
            emptyList()
        }

        ustadCache.assertCanStoreAndRetrieveFileAsCacheHit(
            testFile =testFile,
            testUrl = testUrl,
            mimeType = mimeType,
            expectedContentEncoding = expectContentEncoding,
            requestHeaders = requestHeaders,
        )

        runBlocking { ustadCache.commit() }


        val cacheEntryInDb = runBlocking {
            cacheDb.cacheEntryDao.findEntryAndBodyByKey(Md5Digest().urlKey(testUrl))
        }
        assertNotNull(cacheEntryInDb)
        val expectedPath = if(createLock) {
            cachePaths.persistentPath
        }else {
            cachePaths.cachePath
        }

        assertTrue(cacheEntryInDb.storageUri.startsWith(expectedPath.toString()),
            "Cache entry is stored in expected directory (createLock=$createLock, " +
                    "expected path = $expectedPath, actual dir = ${cacheEntryInDb.storageUri}")

        block(FileCanBeCachedAndRetrievedContext(cacheDb, ustadCache, createdLocks))
    }

    @Test
    fun givenNonCompressableFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndNotCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity"
        )
    }

    @Test
    fun givenLockedEntryStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndSavedInPersistentPath() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity",
            createLock = true,
        )
    }

    @Test
    fun givenEntryNotLocked_whenLockAdded_thenWillBeMovedToPersistentDir() {
        val url = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity"
        ) {
            runBlocking { cache.addRetentionLocks(listOf(EntryLockRequest(url))) }
            val entry = runBlocking { cache.getCacheEntry(url) }
            assertTrue(entry?.storageUri?.startsWith(cachePaths.persistentPath.toString()) == true,
                "After adding lock, entry should be in persistent path")
        }
    }

    @Test
    fun givenEntryLocked_whenLockRemoved_thenWillBeMovedToCacheDir() {
        val url = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity",
            createLock = true,
        ) {
            runBlocking {
                cache.removeRetentionLocks(
                    createdLocks.map {
                        RemoveLockRequest(url, it.second.lockId)
                    }
                )

                val entry = cache.getCacheEntry(url)
                assertTrue(entry?.storageUri?.startsWith(cachePaths.cachePath.toString()) == true,
                    "After adding lock, entry should be in persistent path")
            }
        }
    }

    @Test
    fun givenCompressableFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndBeCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/ustadmobile-epub.js"),
            testUrl = "http://www.server.com/ustadmobile-epub.js",
            mimeType = "application/javascript",
            expectContentEncoding = "gzip",
            requestHeaders = listOf(IHttpHeader.fromNameAndValue("accept-encoding", "gzip, br, deflate"))
        )
    }

    @Test
    fun givenCompressableFileStored_whenRequestMadeWithoutAcceptEncoding_thenWillBeRetrievedAsCacheHitAndBeCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/ustadmobile-epub.js"),
            testUrl = "http://www.server.com/ustadmobile-epub.js",
            mimeType = "application/javascript",
            expectContentEncoding = "identity",
        )
    }

    @Test
    fun givenEmptyFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHit() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFile(),
            testUrl = "http://www.server.com/blank.txt",
            mimeType = "text/plain"
        )
    }

    @Test
    fun givenResponseIsUpdated_whenRetrieved_thenLatestResponseWillBeReturned(){
        val cacheDb = Room.databaseBuilder<UstadCacheDb>(
            tempDir.newFile("cachetest.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build()
        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            xxStringHasher = XXStringHasherCommonJvm(),
            db = cacheDb,
            enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                cacheDb, XXStringHasherCommonJvm()
            ),
        )

        val url = "http://server.com/file.css"
        val payloads = listOf("font-weight: bold", "font-weight: bold !important")
        runBlocking {
            payloads.forEachIndexed { index, payload ->
                ustadCache.store(listOf(
                    iRequestBuilder(url).let {
                        CacheEntryToStore(
                            request = it,
                            response = StringResponse(
                                request = it,
                                mimeType = "text/css",
                                body = payload,
                            )
                        )
                    }
                ))
            }

            val response = ustadCache.retrieve(iRequestBuilder(url))
            val responseBytes = response?.bodyAsUncompressedSourceIfContentEncoded()
                ?.asInputStream()?.readAllBytes()
            val responseStr = responseBytes?.let { String(it) }
            assertEquals(payloads.last(), responseStr)
        }

    }


    @Test
    fun givenEntryNotStored_whenRetrieved_thenWillReturnNull() {
        val cacheDb = Room.databaseBuilder<UstadCacheDb>(
            tempDir.newFile("cachetest.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build()
        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            db = cacheDb,
            xxStringHasher = XXStringHasherCommonJvm(),
            enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                cacheDb, XXStringHasherCommonJvm()
            ),
        )

        val url = "http://server.com/file.css"
        assertNull(runBlocking { ustadCache.retrieve(iRequestBuilder(url)) } )
    }

    @Test
    fun givenResponseIsNotUpdated_whenStored_thenWillUpdateLastAccessAndValidationTime() {
        val cacheDb = Room.databaseBuilder<UstadCacheDb>(
            tempDir.newFile("cachetest.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build()
        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            db = cacheDb,
            xxStringHasher = XXStringHasherCommonJvm(),
            enqueuePinPublicationPrepareUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                cacheDb, XXStringHasherCommonJvm()
            ),
        )

        val url = "http://server.com/file.css"
        val tmpFile = tempDir.newFile().also {
            it.writeText("font-weight: bold")
        }

        val md5Digest = Md5Digest()
        val entryAfterStored = (1..2).map {
            ustadCache.assertCanStoreAndRetrieveFileAsCacheHit(
                testFile = tmpFile,
                testUrl = url,
                mimeType = "text/css"
            )

            runBlocking {
                ustadCache.commit()
                cacheDb.cacheEntryDao.findEntryAndBodyByKey(md5Digest.urlKey(url))
            }
        }


        assertTrue(entryAfterStored.last()!!.lastValidated > entryAfterStored.first()!!.lastValidated,
            message = "Last validated time should be updated after ")

        //Cache tmp directory should not have any leftover files.
        val cacheTmpDir = File(rootDir, "tmpWork")

        assertTrue(cacheTmpDir.exists())
        assertEquals(0, cacheTmpDir.list()!!.size)
    }

    @Test
    fun givenFileCachedAndStored_whenPartialRequestMade_thenWillReceivePartialData() {
        val testUrl = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = testUrl,
            mimeType = "image/png",
            expectContentEncoding = "identity"
        ) {
            val resourceBytes = this::class.java.getResourceAsStream(
                "/testfile1.png")!!.readAllBytes()
            val etag = runBlocking {
                cache.retrieve(iRequestBuilder(testUrl))?.headers?.get("etag")
            }
            assertNotNull(etag)

            val partialResponse = runBlocking {
                cache.retrieve(iRequestBuilder(testUrl) {
                    header("Range", "bytes=1000-")
                    header("If-Range", etag)
                })
            }
            assertNotNull(partialResponse)
            assertEquals(206, partialResponse.responseCode)

            val partialResponseInput = partialResponse.bodyAsSource()!!.asInputStream()

            val combinedBytes = SequenceInputStream(
                RangeInputStream(ByteArrayInputStream(resourceBytes), 0, 999),
                partialResponseInput
            ).readAllBytes()

            assertTrue(resourceBytes.contentEquals(combinedBytes),
                "Combined partial response data should match original resource data")
        }
    }

    @Test
    fun givenFileCachedAndStored_whenPartialRequestMadeIfRangeNotMatched_thenWillReceiveFullResponse() {
        val testUrl = "http://www.server.com/file.png"
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = testUrl,
            mimeType = "image/png",
            expectContentEncoding = "identity"
        ) {
            val resourceBytes = this::class.java.getResourceAsStream(
                "/testfile1.png")!!.readAllBytes()
            val fullResponse = runBlocking {
                cache.retrieve(iRequestBuilder(testUrl) {
                    header("Range", "bytes=1000-")
                    header("If-Range", "something-else")
                })
            }
            assertNotNull(fullResponse)
            assertEquals(200, fullResponse.responseCode,
                "When if-range did not match etag, full response should be returned")

            val responseBytes = fullResponse.bodyAsSource()!!.asInputStream().readAllBytes()
            assertTrue(resourceBytes.contentEquals(responseBytes),
                "When if-range did not match actual etag, returned full response")
        }
    }


}