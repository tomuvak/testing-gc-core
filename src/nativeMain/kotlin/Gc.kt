package com.tomuvak.testing.gc.core

import kotlin.native.internal.GC

actual fun whenCollectingGarbage() = GC.collect()
