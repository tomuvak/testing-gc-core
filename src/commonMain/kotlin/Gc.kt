package com.tomuvak.testing.gc.core

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * On supported platforms (currently JVM and Native), triggers garbage collection synchronously and returns `true`. Not
 * guaranteed to actually work, but empirically seems to (note that a return value of `true` doesn't assure garbage has
 * actually been collected – it only signals that the functionality is supposed to be supported on the platform).
 *
 * On unsupported platforms (currently, and perhaps indefinitely, JS), does nothing and returns `false`.
 *
 * [tryToAchieveByForcingGc] should be considered as an alternative if a solution which covers JS as well is desired.
 */
expect fun whenCollectingGarbage(): Boolean

/**
 * Evaluates the given [condition] at least once and potentially multiple times (up to the given [maxNumAttempts], which
 * must be at least `2`, on platforms that do not support direct triggering of garbage collection – namely JS, or twice
 * on platforms that do, regardless of [maxNumAttempts]) and returns `true` as soon as the [condition] evaluates to
 * `true`, or `false` if all evaluations resulted in `false`. Tries to trigger garbage collection between consecutive
 * evaluations of [condition], either directly by invoking [whenCollectingGarbage] internally on platforms where it's
 * supported or, on other platforms (= JS), indirectly by performing a memory-intensive task in the hope that garbage
 * collection will be triggered (the task allocates an array of [Int]s of length [dataSizeInInts] – which cannot be
 * negative, and is irrelevant for non-JS platforms).
 *
 * Can only be called from a coroutine.
 *
 * This function is more cumbersome than [whenCollectingGarbage], both in usage and in internal operation (which,
 * depending on platform and circumstances, may be memory- and computation-intensive), and is recommended to only be
 * used when targeting (also) a platform where [whenCollectingGarbage] isn't an option (= JS).
 */
suspend fun tryToAchieveByForcingGc(
    maxNumAttempts: Int = 9,
    dataSizeInInts: Int = 4 * 1024 * 1024,
    condition: () -> Boolean
): Boolean {
    require(maxNumAttempts >= 2) { "maxNumAttempts must be at least 2 (was $maxNumAttempts)" }
    require(dataSizeInInts >= 0) { "dataSizeInInts cannot be negative (was $dataSizeInInts)" }
    if (condition()) return true
    if (whenCollectingGarbage()) return condition()

    var result: Boolean
    coroutineScope {
        var data = List(dataSizeInInts) { it }
        delay(1)
        for (i in 1 until maxNumAttempts - 1) {
            if (condition()) {
                result = true
                return@coroutineScope
            }
            delay(1)
            data = data.map { it }
        }
        result = condition()
    }
    return result
}
