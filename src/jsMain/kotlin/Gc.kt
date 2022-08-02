package com.tomuvak.testing.gc.core

actual fun whenCollectingGarbage(): Unit =
    throw NotImplementedError("Triggering garbage collection not supported on platform")
