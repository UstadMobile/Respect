package world.respect.libutil.ext

import io.ktor.http.Url
import org.junit.Test
import kotlin.test.assertEquals

class UrlResolveTest {


    /**
     * Basic peace of mind test scenarios
     */
    @Test
    fun testResolution() {
        assertEquals("https://server.ustadmobile.app/",
            Url("https://server.ustadmobile.app/somewhere/").resolve("../").toString())
        assertEquals("https://www.google.com/",
            Url("https://server.ustadmobile.app/somewhere/")
                .resolve("https://www.google.com/").toString())
        assertEquals("https://server.ustadmobile.app/somewhere/link.json",
            Url("https://server.ustadmobile.app/somewhere/")
                .resolve("link.json").toString())
    }


}