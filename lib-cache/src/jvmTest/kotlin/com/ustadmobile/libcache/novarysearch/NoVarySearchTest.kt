package com.ustadmobile.libcache.novarysearch

import kotlin.test.Test
import kotlin.test.assertEquals

class NoVarySearchTest {

    @Test
    fun givenValidHeaders_whenParsed_willReturnExpectedDataClass() {
        //Examples as per https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/No-Vary-Search#examples
        assertEquals(
            NoVarySearch(keyOrder = true),
            NoVarySearch.parse("key-order")
        )

        assertEquals(
            NoVarySearch(params = listOf("id")),
            NoVarySearch.parse("params=(\"id\")")
        )

        assertEquals(
            NoVarySearch(params = listOf("id", "order", "lang")),
            NoVarySearch.parse("params=(\"id\" \"order\" \"lang\")")
        )

        assertEquals(
            NoVarySearch(params = emptyList()),
            NoVarySearch.parse("params")
        )

        assertEquals(
            NoVarySearch(params = emptyList(), except = listOf("id")),
            NoVarySearch.parse("params, except=(\"id\")")
        )
    }

}