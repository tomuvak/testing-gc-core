package com.tomuvak.testing.gc.core

import kotlin.test.Test
import kotlin.test.assertFalse

class GcJsTest {
    @Test fun whenCollectingGarbageReturnsFalse() = assertFalse(whenCollectingGarbage())
}
