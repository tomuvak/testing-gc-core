@file:JvmName("GcJvmKt")

package com.tomuvak.testing.gc.core

actual fun whenCollectingGarbage() = System.gc()
