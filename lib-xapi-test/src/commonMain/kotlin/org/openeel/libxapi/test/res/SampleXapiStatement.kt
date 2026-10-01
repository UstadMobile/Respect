package org.openeel.libxapi.test.res

import kotlinx.serialization.json.JsonObject

data class SampleXapiStatement(
    val jsonObject: JsonObject,
    val string: String,
    val name: String,
)