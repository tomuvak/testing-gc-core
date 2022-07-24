package com.tomuvak.testing.gc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

actual fun whenCollectingGarbage() { error("Triggering garbage collection not supported on platform") }

actual suspend fun CoroutineScope.tryToAchieveByForcingGc(condition: () -> Boolean): Boolean {
    var result: Boolean

    coroutineScope {
        var data = List(64 * 1024 * 1024) { it }
        for (i in 0 until 4) {
            if (condition()) {
                result = true
                break
            }
            delay(1)
            data = data.map { it * 2 }
        }
        result = condition()
    }

    return result
}
