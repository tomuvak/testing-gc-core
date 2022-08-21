package com.tomuvak.testing.gc.core

import kotlin.native.internal.GC

actual fun whenCollectingGarbage(): Boolean {
    GC.collect()
    return true
}
