@file:JvmName("GcJvmKt")

package com.tomuvak.testing.gc

actual fun whenCollectingGarbage() = System.gc()
