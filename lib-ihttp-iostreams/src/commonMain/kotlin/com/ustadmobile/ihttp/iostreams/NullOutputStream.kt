package com.ustadmobile.ihttp.iostreams

import java.io.OutputStream

class NullOutputStream: OutputStream() {

    override fun write(p0: Int) {
        //do nothing
    }

    override fun write(p0: ByteArray) {
        //do nothing
    }

    override fun write(p0: ByteArray, p1: Int, p2: Int) {
        //do nothing
    }
}