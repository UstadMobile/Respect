package com.ustadmobile.ihttp.ext

fun String.requirePostfix(
    postFix: String,
    ignoreCase: Boolean = false
) = if(this.endsWith(postFix, ignoreCase)) this else "$this$postFix"

fun String.requirePrefix(
    prefix: String,
    ignoreCase: Boolean = false
) = if(this.startsWith(prefix, ignoreCase)) this else "$prefix$this"
