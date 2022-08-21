package com.tomuvak.testing.gc.core

import kotlin.test.Test
import kotlin.test.assertTrue

class GcNativeTest {
    @Test fun whenCollectingGarbageReturnsTrue() = assertTrue(whenCollectingGarbage())
}
