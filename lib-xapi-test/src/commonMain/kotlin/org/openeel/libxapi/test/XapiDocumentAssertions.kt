package org.openeel.libxapi.test

import world.respect.lib.xapi.model.XapiDocument
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


suspend fun assertXapiDocumentsEqual(
    expected: XapiDocument,
    actual: XapiDocument,
    message: String? = null,
) {
    assertEquals(expected.type, actual.type)
    assertContentEquals(
        expected.contentsAsByteArray(),
        actual.contentsAsByteArray()
    )
    assertEquals(expected.updated, actual.updated)
}