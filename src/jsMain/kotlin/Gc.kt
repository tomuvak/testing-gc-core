package com.tomuvak.testing.gc

actual fun whenCollectingGarbage(): Unit =
    throw NotImplementedError("Triggering garbage collection not supported on platform")
