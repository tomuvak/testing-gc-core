package com.tomuvak.testing.gc

actual fun whenCollectingGarbage() { error("Triggering garbage collection not supported on platform") }
