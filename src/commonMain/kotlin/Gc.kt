package com.tomuvak.testing.gc.core

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * Triggers garbage collection synchronously. Not guaranteed to actually work in JVM and Native, but seems to.
 * Not supported on JS (at least currently, and perhaps indefinitely), and will throw at run time if attempted.
 * [tryToAchieveByForcingGc] should be considered as an alternative if a solution which covers JS as well is desired.
 */
expect fun whenCollectingGarbage()

/**
 * Evaluates the given [condition] at least once and potentially multiple times (the exact number of times is
 * indeterminate and implementation/platform-specific, but bound) and returns `true` as soon as the [condition]
 * evaluates to `true`, or `false` if all evaluations resulted in `false`. Might try to trigger garbage collection along
 * the process, either directly (e.g. by invoking [whenCollectingGarbage] on platforms where it's supported) or
 * indirectly (e.g. by performing a memory-intensive task in the hope that garbage collection will be triggered).
 *
 * Can only be called from a coroutine.
 *
 * This function is more cumbersome than [whenCollectingGarbage], both in usage and in internal operation (which,
 * depending on platform and circumstances, may be memory- and computation-intensive), and is recommended to only be
 * used when targeting (also) a platform where [whenCollectingGarbage] isn't an option (= JS).
 */
suspend fun tryToAchieveByForcingGc(condition: () -> Boolean): Boolean {
    if (condition()) return true

    var garbageHasBeenCollected = false
    try {
        whenCollectingGarbage()
        garbageHasBeenCollected = true
    } catch (_: NotImplementedError) {}

    if (garbageHasBeenCollected) return condition()

    var result: Boolean
    coroutineScope {
        var data = List(4 * 1024 * 1024) { it }
        delay(1)
        for (i in 0 until 7) {
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
