@file:JvmName("GcJvmKt")

package com.tomuvak.testing.gc.core

actual fun whenCollectingGarbage(): Boolean {
    System.gc()
    return true
}
