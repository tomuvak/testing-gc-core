package com.tomuvak.testing.gc.core

import com.tomuvak.testing.assertions.assertFailsWithTypeAndMessageContaining
import com.tomuvak.testing.assertions.mootProvider
import com.tomuvak.testing.coroutines.asyncTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GcTest {
    @Test fun tryToAchieveByForcingGcRequiresMaxNumAttemptsToBeAtLeastTwo() = asyncTest {
        for (maxNumAttempts in -2 until 2)
            assertFailsWithTypeAndMessageContaining<IllegalArgumentException>(2, maxNumAttempts) {
                tryToAchieveByForcingGc(maxNumAttempts, condition=mootProvider)
            }
    }

    @Test fun tryToAchieveByForcingGcReturnsTrueAsSoonAsConditionEvaluatesToTrue() = asyncTest {
        for (numAttempts in 1..5) if (canAttemptThatManyTimesOnPlatform(numAttempts))
            for (maxNumAttempts in numAttempts.coerceAtLeast(2)..7) {
                var numEvaluations = 0
                assertTrue(tryToAchieveByForcingGc(maxNumAttempts) { ++numEvaluations == numAttempts })
                assertEquals(numEvaluations, numAttempts)
            }
    }

    @Test fun tryToAchieveByForcingGcOnlyAttemptsTheSpecifiedNumberOfTimes() = asyncTest {
        for (maxNumAttempts in 2..7) {
            var numEvaluations = 0
            assertFalse(tryToAchieveByForcingGc(maxNumAttempts) {
                numEvaluations++
                false
            })
            assertEquals(if (canAttemptThatManyTimesOnPlatform(maxNumAttempts)) maxNumAttempts else 2, numEvaluations)
        }
    }

    @Test fun tryToAchieveByForcingGcRequiresDataSizeInIntsToBeNonNegative() = asyncTest {
        for (dataSizeInInts in listOf(-1048576, -1024, -2, -1))
            assertFailsWithTypeAndMessageContaining<IllegalArgumentException>("negative", dataSizeInInts) {
                tryToAchieveByForcingGc(dataSizeInInts=dataSizeInInts, condition=mootProvider)
            }
    }

    private fun canAttemptThatManyTimesOnPlatform(numAttempts: Int): Boolean =
        numAttempts <= 2 || !whenCollectingGarbage()
}
