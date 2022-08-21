package com.tomuvak.testing.gc.core

import kotlin.test.Test
import kotlin.test.assertTrue

class GcJvmTest {
    @Test fun whenCollectingGarbageReturnsTrue() = assertTrue(whenCollectingGarbage())
}
