package com.tomuvak.testing.gc.core

import com.tomuvak.testing.coroutines.asyncTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GcTest {
    @Test fun tryToAchieveByForcingGcOnlyEvaluatesTrueConditionOnceAndReturnsTrue() = asyncTest {
        var numEvaluations = 0
        assertTrue(tryToAchieveByForcingGc {
            numEvaluations++
            true
        })
        assertEquals(1, numEvaluations)
    }

    @Test fun tryToAchieveByForcingGcReturnsFalseWhenConditionIsAlwaysFalse() = asyncTest {
        assertFalse(tryToAchieveByForcingGc { false })
    }

    @Test fun tryToAchieveByForcingGcOnlyAttemptsTheSpecifiedNumberOfTimes() = asyncTest {
        var numEvaluations = 0
        assertFalse(tryToAchieveByForcingGc {
            numEvaluations++
            false
        })
        assertEquals(maxNumAttemptsForPlatform, numEvaluations)
    }

    private val maxNumAttemptsForPlatform: Int get() = if (whenCollectingGarbage()) 2 else 9
}
