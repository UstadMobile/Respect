package com.ustadmobile.libcache.io

import kotlinx.io.RawSource
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import java.io.File
import java.io.FileInputStream
import kotlin.use

fun FileSystem.rangeSource(path: Path, fromByte: Long, toByte: Long): RawSource {
    val fileIn = FileInputStream(path.toString())

    return RangeInputStream(
        src = fileIn,
        fromByte  = fromByte,
        toByte = toByte,
    ).asSource()
}


fun FileSystem.lastModified(path: Path): Long {
    return File(path.toString()).lastModified()
}


fun FileSystem.moveWithFallback(source: Path, destination: Path) {
    try {
        atomicMove(source, destination)
    }catch(e: Exception) {
        //Might be on different volumes
        source(source).buffered().use { bufferedSource ->
            sink(destination).use { sink ->
                bufferedSource.transferTo(sink)
            }
        }
        delete(source)
    }
}