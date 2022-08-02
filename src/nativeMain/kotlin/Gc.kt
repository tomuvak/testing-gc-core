package com.tomuvak.testing.gc

import kotlin.native.internal.GC

actual fun whenCollectingGarbage() = GC.collect()
