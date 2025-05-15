package world.respect.domain.opds.validator

import kotlinx.serialization.json.Json
import kotlin.test.*
import world.respect.domain.opds.model.OpdsFeed
import world.respect.domain.opds.model.OpdsPublication

class OpdsValidatorTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    private val validator = OpdsValidator()

    private fun readResource(path: String): String {
        return this::class.java.classLoader.getResource(path)?.readText()
            ?: error("File not found: $path")
    }

    @Test
    fun validFeedPasses() {
        val jsonString = readResource("sample-feed.json")
        val feed = json.decodeFromString(OpdsFeed.serializer(), jsonString)
        val result = validator.validateFeed(feed)
        assertTrue(result.isSuccess, "sample-feed.json should pass validation")
    }

    @Test
    fun invalidFeedFails() {
        val jsonString = readResource("invalid-catalog.json")
        val feed = json.decodeFromString(OpdsFeed.serializer(), jsonString)
        val result = validator.validateFeed(feed)
        assertFalse(result.isSuccess, "invalid-catalog.json should fail validation")
    }

    @Test
    fun validPublicationPasses() {
        val jsonString = readResource("sample-publication.json")
        val publication = json.decodeFromString(OpdsPublication.serializer(), jsonString)
        val result = validator.validatePublication(publication)
        assertTrue(result.isSuccess, "sample-publication.json should pass validation")
    }

    @Test
    fun feedWithArrayRelPasses() {
        val jsonString = readResource("sample-with-array-rel.json")
        val feed = json.decodeFromString(OpdsFeed.serializer(), jsonString)
        val result = validator.validateFeed(feed)
        assertTrue(result.isSuccess, "sample-with-array-rel.json should support array rel field")
    }
}
