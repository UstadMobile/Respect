package com.ustadmobile.libcache.novarysearch

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals

class UrlBuilderApplyNoVarySearchTest {

    @Test
    fun givenNoVaryParams_whenApplied_thenUrlIsNormalized() {
        assertEquals(
            Url("http://localhost/users"),
            URLBuilder(Url("http://localhost/users?id=123")).apply {
                normalizeForNoVarySearch(NoVarySearch(params = listOf("id")))
            }.build()
        )

        assertEquals(
            Url("http://localhost/users?otherParam=value"),
            URLBuilder(Url("http://localhost/users?id=123&otherParam=value")).apply {
                normalizeForNoVarySearch(NoVarySearch(params = listOf("id")))
            }.build()
        )

        assertEquals(
            Url("http://localhost/users"),
            URLBuilder(Url("http://localhost/users?id=123&order=asc&lang=en")).apply {
                normalizeForNoVarySearch(NoVarySearch(params = listOf("id", "order", "lang")))
            }.build()
        )

        assertEquals(
            Url("http://localhost/users"),
            URLBuilder(Url("http://localhost/users?id=123&order=asc&lang=en")).apply {
                normalizeForNoVarySearch(NoVarySearch(params = listOf()))
            }.build()
        )

        assertEquals(
            Url("http://localhost/users?id=123"),
            URLBuilder(Url("http://localhost/users?id=123&order=asc&lang=en")).apply {
                normalizeForNoVarySearch(NoVarySearch(params = listOf(), except = listOf("id")))
            }.build()
        )

        //When key order is used, then parameters are sorted alphabetically to ensure they will match
        //even if the order changes.
        assertEquals(
            Url("http://localhost/users?id=123&lang=en&order=asc"),
            URLBuilder(Url("http://localhost/users?id=123&order=asc&lang=en")).apply {
                normalizeForNoVarySearch(NoVarySearch(keyOrder = true))
            }.build()
        )
    }

}